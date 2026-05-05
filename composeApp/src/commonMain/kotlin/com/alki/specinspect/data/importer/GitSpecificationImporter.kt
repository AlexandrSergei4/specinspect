package com.alki.specinspect.data.importer

import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.localizedError

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
    suspend fun loadRepository(repositoryUrl: String, userAccessToken: String): AvailableGitRepository
    suspend fun loadBranches(repositoryUrl: String, userAccessToken: String): List<String>
    suspend fun importSpecification(request: GitSpecificationImportRequest): List<ImportedSpecFile>
}

class UnsupportedGitSpecificationImporter : GitSpecificationImporter {

    override suspend fun loadRepositories(userAccessToken: String): List<AvailableGitRepository> {
        localizedError(AppTextKey.ErrorImportUnsupported)
    }

    override suspend fun loadRepository(repositoryUrl: String, userAccessToken: String): AvailableGitRepository {
        localizedError(AppTextKey.ErrorImportUnsupported)
    }

    override suspend fun loadBranches(repositoryUrl: String, userAccessToken: String): List<String> {
        localizedError(AppTextKey.ErrorImportUnsupported)
    }

    override suspend fun importSpecification(request: GitSpecificationImportRequest): List<ImportedSpecFile> {
        localizedError(AppTextKey.ErrorImportUnsupported)
    }
}
