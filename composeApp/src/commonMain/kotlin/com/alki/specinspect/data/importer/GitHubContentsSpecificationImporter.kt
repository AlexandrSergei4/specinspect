package com.alki.specinspect.data.importer

import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.localizedError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GitHubContentsSpecificationImporter(
    private val client: HttpClient = HttpClient(),
) : GitSpecificationImporter {

    private val json = Json { ignoreUnknownKeys = true }
    private val repositoriesCache = mutableMapOf<String, List<AvailableGitRepository>>()
    private val branchesCache = mutableMapOf<BranchesCacheKey, List<String>>()

    override suspend fun loadRepositories(userAccessToken: String): List<AvailableGitRepository> {
        val token = userAccessToken.trim()
        if (token.isEmpty()) localizedError(AppTextKey.ErrorTokenRequired)
        repositoriesCache[token]?.let { return it }

        val repositories = loadPagedArray { page ->
            client.get("https://api.github.com/user/repos") {
                parameter("sort", "updated")
                parameter("affiliation", "owner,collaborator,organization_member")
                parameter("per_page", PAGE_SIZE)
                parameter("page", page)
                applyGitHubHeaders(token)
            }
        }
            .map { item ->
                AvailableGitRepository(
                    fullName = item["full_name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                    defaultBranch = item["default_branch"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                )
            }
            .filter { it.fullName.isNotBlank() }
            .sortedBy { it.fullName.lowercase() }
        repositoriesCache[token] = repositories
        return repositories
    }

    override suspend fun loadBranches(repositoryUrl: String, userAccessToken: String): List<String> {
        val token = userAccessToken.trim()
        if (token.isEmpty()) localizedError(AppTextKey.ErrorTokenRequired)

        val repository = parseGitHubRepository(repositoryUrl)
        val cacheKey = BranchesCacheKey(token = token, repository = repository.owner + "/" + repository.repo)
        branchesCache[cacheKey]?.let { return it }

        val branches = loadPagedArray { page ->
            client.get("https://api.github.com/repos/${repository.owner}/${repository.repo}/branches") {
                parameter("per_page", PAGE_SIZE)
                parameter("page", page)
                applyGitHubHeaders(token)
            }
        }
            .mapNotNull { item -> item["name"]?.jsonPrimitive?.contentOrNull }
            .filter { it.isNotBlank() }
            .sortedBy { it.lowercase() }
        branchesCache[cacheKey] = branches
        return branches
    }

    override suspend fun importSpecification(request: GitSpecificationImportRequest): List<ImportedSpecFile> {
        val repository = parseGitHubRepository(request.repositoryUrl)
        val branch = request.branch.trim()
        val specificationPath = normalizeSpecificationPath(request.specificationPath)
        val token = request.userAccessToken.trim()

        if (branch.isEmpty()) localizedError(AppTextKey.ErrorBranchRequired)
        if (token.isEmpty()) localizedError(AppTextKey.ErrorTokenRequired)

        return collectSpecFiles(
            repository = repository,
            directoryPath = specificationPath,
            rootPath = specificationPath,
            branch = branch,
            token = token,
        )
            .sortedBy { it.name.lowercase() }
    }

    private suspend fun collectSpecFiles(
        repository: GitHubRepositoryId,
        directoryPath: String,
        rootPath: String,
        branch: String,
        token: String,
    ): List<ImportedSpecFile> {
        val result = mutableListOf<ImportedSpecFile>()
        listDirectoryEntries(
            repository = repository,
            directoryPath = directoryPath,
            branch = branch,
            token = token,
        )
            .sortedBy { it.path.lowercase() }
            .forEach { entry ->
                when {
                    entry.type == "file" && entry.name.equals("spec.md", ignoreCase = true) -> {
                        result += ImportedSpecFile(
                            name = resolveImportedSpecName(rootPath = rootPath, filePath = entry.path),
                            content = loadFileContent(
                                repository = repository,
                                filePath = entry.path,
                                branch = branch,
                                token = token,
                            ),
                            path = entry.path,
                        )
                    }
                    entry.type == "dir" -> {
                        result += collectSpecFiles(
                            repository = repository,
                            directoryPath = entry.path,
                            rootPath = rootPath,
                            branch = branch,
                            token = token,
                        )
                    }
                }
            }
        return result
    }

    private suspend fun listDirectoryEntries(
        repository: GitHubRepositoryId,
        directoryPath: String,
        branch: String,
        token: String,
    ): List<GitHubContentEntry> {
        val response = client.get(buildContentsUrl(repository, directoryPath)) {
            parameter("ref", branch)
            header(HttpHeaders.Accept, "application/vnd.github.object+json")
            applyGitHubHeaders(token, includeAccept = false)
        }
        val body = response.bodyAsText()
        ensureSuccess(response.status, body)
        val payload = json.parseToJsonElement(body).jsonObject
        val entries = payload["entries"]?.jsonArray
            ?: localizedError(AppTextKey.ErrorGitHubUnexpectedDirectoryResponse, directoryPath)
        return entries.map { element ->
            element.jsonObject.toContentEntry()
        }
    }

    private suspend fun loadFileContent(
        repository: GitHubRepositoryId,
        filePath: String,
        branch: String,
        token: String,
    ): String {
        val response = client.get(buildContentsUrl(repository, filePath)) {
            parameter("ref", branch)
            header(HttpHeaders.Accept, "application/vnd.github.raw+json")
            applyGitHubHeaders(token, includeAccept = false)
        }
        val body = response.bodyAsText()
        ensureSuccess(response.status, body)
        return body
    }

    private suspend fun loadPagedArray(
        request: suspend (page: Int) -> io.ktor.client.statement.HttpResponse,
    ): List<JsonObject> {
        val result = mutableListOf<JsonObject>()
        var page = 1
        while (true) {
            val response = request(page)
            val body = response.bodyAsText()
            ensureSuccess(response.status, body)
            val items = json.parseToJsonElement(body).jsonArray.map { it.jsonObject }
            result += items
            if (!hasNextPage(response.headers) || items.isEmpty()) break
            page++
        }
        return result
    }

    private fun hasNextPage(headers: Headers): Boolean {
        val linkHeader = headers[HttpHeaders.Link] ?: return false
        return linkHeader.split(',').any { part -> part.contains("rel=\"next\"") }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyGitHubHeaders(
        token: String,
        includeAccept: Boolean = true,
    ) {
        header(HttpHeaders.Authorization, "Bearer $token")
        if (includeAccept) {
            header(HttpHeaders.Accept, "application/vnd.github+json")
        }
        header("X-GitHub-Api-Version", "2026-03-10")
        header(HttpHeaders.UserAgent, "SpecInspect")
    }

    private fun ensureSuccess(status: HttpStatusCode, body: String) {
        if (status.isSuccess()) return
        val apiMessage = parseApiMessage(body)
        val message = when (status) {
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden -> localizedError(AppTextKey.ErrorGitHubTokenOrAccess)
            HttpStatusCode.NotFound -> localizedError(AppTextKey.ErrorGitHubNotFound)
            else -> apiMessage ?: localizedError(AppTextKey.ErrorGitHubApi, status.value.toString())
        }
        throw IllegalStateException(message)
    }

    private fun parseApiMessage(body: String): String? =
        runCatching {
            json.parseToJsonElement(body)
                .jsonObject["message"]
                ?.jsonPrimitive
                ?.contentOrNull
        }.getOrNull()

    private fun buildContentsUrl(repository: GitHubRepositoryId, path: String): String {
        val normalizedPath = normalizeGitHubPath(path)
        return if (normalizedPath.isEmpty()) {
            "https://api.github.com/repos/${repository.owner}/${repository.repo}/contents"
        } else {
            "https://api.github.com/repos/${repository.owner}/${repository.repo}/contents/$normalizedPath"
        }
    }
}

internal data class GitHubRepositoryId(
    val owner: String,
    val repo: String,
)

private data class GitHubContentEntry(
    val type: String,
    val name: String,
    val path: String,
)

private data class BranchesCacheKey(
    val token: String,
    val repository: String,
)

internal fun parseGitHubRepository(repositoryUrl: String): GitHubRepositoryId {
    val value = repositoryUrl.trim()
    if (value.isEmpty()) localizedError(AppTextKey.ErrorGitHubRepositoryRequired)
    if ((value.contains("://") || value.startsWith("git@")) && !value.contains("github.com")) {
        localizedError(AppTextKey.ErrorGitHubOnlySupported)
    }

    val normalized = when {
        value.startsWith("git@github.com:") -> value.removePrefix("git@github.com:")
        value.startsWith("ssh://git@github.com/") -> value.removePrefix("ssh://git@github.com/")
        value.startsWith("https://github.com/") -> value.removePrefix("https://github.com/")
        value.startsWith("http://github.com/") -> value.removePrefix("http://github.com/")
        value.startsWith("github.com/") -> value.removePrefix("github.com/")
        else -> value
    }

    val parts = normalized
        .trim('/')
        .split('/')
        .filter { it.isNotBlank() }

    if (parts.size < 2) {
        localizedError(AppTextKey.ErrorGitHubRepositoryFormatRequired)
    }

    return GitHubRepositoryId(
        owner = parts[0],
        repo = parts[1].removeSuffix(".git"),
    )
}

internal fun normalizeSpecificationPath(specificationPath: String): String {
    val normalized = normalizeGitHubPath(specificationPath)
    if (normalized.isEmpty()) localizedError(AppTextKey.ErrorSpecificationPathRequired)
    return if (normalized.endsWith("spec.md")) normalized.substringBeforeLast("/", "") else normalized
}

internal fun resolveImportedSpecName(rootPath: String, filePath: String): String {
    val normalizedRoot = normalizeGitHubPath(rootPath)
    val normalizedFile = normalizeGitHubPath(filePath)
    val parentPath = normalizedFile.substringBeforeLast("/", "")
    val relativeParent = when {
        parentPath == normalizedRoot -> ""
        normalizedRoot.isNotEmpty() && parentPath.startsWith("$normalizedRoot/") -> parentPath.removePrefix("$normalizedRoot/")
        else -> parentPath
    }
    return when {
        relativeParent.isNotBlank() -> relativeParent
        parentPath.isNotBlank() -> parentPath.substringAfterLast('/')
        else -> "spec"
    }
}

private fun JsonObject.toContentEntry(): GitHubContentEntry = GitHubContentEntry(
    type = this["type"]?.jsonPrimitive?.contentOrNull.orEmpty(),
    name = this["name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
    path = this["path"]?.jsonPrimitive?.contentOrNull.orEmpty(),
)

private fun normalizeGitHubPath(path: String): String =
    path.trim().replace('\\', '/').trim('/')

private const val PAGE_SIZE = 100
