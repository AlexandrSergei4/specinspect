package com.alki.specinspect.localization

enum class AppTextKey {
    AddSpecStatusTokenHint,
    AddSpecStatusRepositoriesLoading,
    AddSpecStatusRepositoriesEmpty,
    AddSpecStatusSelectRepository,
    AddSpecStatusBranchesLoading,
    AddSpecStatusBranchesEmpty,
    ErrorSpecNameRequired,
    ErrorNoSpecFiles,
    ErrorSubspecNameMissing,
    ErrorOpenSpecRequirementParseFailed,
    ErrorSpecKitOrRequirementParseFailed,
    ErrorTokenRequired,
    ErrorBranchRequired,
    ErrorGitHubUnexpectedDirectoryResponse,
    ErrorGitHubTokenOrAccess,
    ErrorGitHubNotFound,
    ErrorGitHubApi,
    ErrorGitHubRepositoryRequired,
    ErrorGitHubOnlySupported,
    ErrorGitHubRepositoryFormatRequired,
    ErrorSpecificationPathRequired,
    ErrorAddSpecFailed,
    ErrorRepositoriesLoadFailed,
    ErrorBranchesLoadFailed,
    ErrorImportUnsupported,
    ErrorInvalidTokenData,
}

sealed interface AppText {
    data class Resource(
        val key: AppTextKey,
        val args: List<String> = emptyList(),
    ) : AppText

    data class Raw(val value: String) : AppText
}

class LocalizedException(
    val key: AppTextKey,
    val args: List<String> = emptyList(),
) : IllegalStateException()

fun appText(key: AppTextKey, vararg args: String): AppText =
    AppText.Resource(key, args.toList())

fun localizedError(key: AppTextKey, vararg args: String): Nothing =
    throw LocalizedException(key, args.toList())

fun Throwable.toAppText(fallbackKey: AppTextKey): AppText =
    when (this) {
        is LocalizedException -> AppText.Resource(key, args)
        else -> message
            ?.takeIf { it.isNotBlank() }
            ?.let(AppText::Raw)
            ?: AppText.Resource(fallbackKey)
    }
