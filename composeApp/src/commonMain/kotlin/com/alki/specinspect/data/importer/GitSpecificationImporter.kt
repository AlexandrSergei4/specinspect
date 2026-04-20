package com.alki.specinspect.data.importer

data class GitSpecificationImportRequest(
    val repositoryUrl: String,
    val branch: String,
    val specificationPath: String,
    val userAccessToken: String,
)

data class ImportedSpecFile(
    val name: String,
    val content: String,
    val path: String = "",
)

data class AvailableGitRepository(
    val fullName: String,
    val defaultBranch: String,
)

interface GitSpecificationImporter {
    suspend fun loadRepositories(userAccessToken: String): List<AvailableGitRepository>
    suspend fun loadBranches(repositoryUrl: String, userAccessToken: String): List<String>
    suspend fun importSpecification(request: GitSpecificationImportRequest): List<ImportedSpecFile>
}

class UnsupportedGitSpecificationImporter(
    private val message: String = "Импорт спецификации не настроен на этой платформе",
) : GitSpecificationImporter {

    override suspend fun loadRepositories(userAccessToken: String): List<AvailableGitRepository> {
        throw IllegalStateException(message)
    }

    override suspend fun loadBranches(repositoryUrl: String, userAccessToken: String): List<String> {
        throw IllegalStateException(message)
    }

    override suspend fun importSpecification(request: GitSpecificationImportRequest): List<ImportedSpecFile> {
        throw IllegalStateException(message)
    }
}
