package com.alki.specinspect.localization

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource as composeStringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.add_spec_status_branches_empty
import specinspect.composeapp.generated.resources.add_spec_status_branches_loading
import specinspect.composeapp.generated.resources.add_spec_status_repositories_empty
import specinspect.composeapp.generated.resources.add_spec_status_repositories_loading
import specinspect.composeapp.generated.resources.add_spec_status_select_repository
import specinspect.composeapp.generated.resources.add_spec_status_token_hint
import specinspect.composeapp.generated.resources.error_add_spec_failed
import specinspect.composeapp.generated.resources.error_branch_required
import specinspect.composeapp.generated.resources.error_branches_load_failed
import specinspect.composeapp.generated.resources.error_github_api
import specinspect.composeapp.generated.resources.error_github_not_found
import specinspect.composeapp.generated.resources.error_github_only_supported
import specinspect.composeapp.generated.resources.error_github_repository_format_required
import specinspect.composeapp.generated.resources.error_github_repository_required
import specinspect.composeapp.generated.resources.error_github_token_or_access
import specinspect.composeapp.generated.resources.error_github_unexpected_directory_response
import specinspect.composeapp.generated.resources.error_import_unsupported
import specinspect.composeapp.generated.resources.error_invalid_token_data
import specinspect.composeapp.generated.resources.error_no_spec_files
import specinspect.composeapp.generated.resources.error_openspec_requirement_parse_failed
import specinspect.composeapp.generated.resources.error_repositories_load_failed
import specinspect.composeapp.generated.resources.error_spec_kit_or_requirement_parse_failed
import specinspect.composeapp.generated.resources.error_spec_name_required
import specinspect.composeapp.generated.resources.error_specification_path_required
import specinspect.composeapp.generated.resources.error_subspec_name_missing
import specinspect.composeapp.generated.resources.error_token_required

@Composable
fun AppText.resolve(): String = when (this) {
    is AppText.Raw -> value
    is AppText.Resource -> stringResource(key, args)
}

@Composable
fun stringResource(key: AppTextKey, args: List<String> = emptyList()): String =
    when (key) {
        AppTextKey.AddSpecStatusTokenHint -> composeStringResource(Res.string.add_spec_status_token_hint)
        AppTextKey.AddSpecStatusRepositoriesLoading -> composeStringResource(Res.string.add_spec_status_repositories_loading)
        AppTextKey.AddSpecStatusRepositoriesEmpty -> composeStringResource(Res.string.add_spec_status_repositories_empty)
        AppTextKey.AddSpecStatusSelectRepository -> composeStringResource(Res.string.add_spec_status_select_repository)
        AppTextKey.AddSpecStatusBranchesLoading -> composeStringResource(Res.string.add_spec_status_branches_loading)
        AppTextKey.AddSpecStatusBranchesEmpty -> composeStringResource(Res.string.add_spec_status_branches_empty)
        AppTextKey.ErrorSpecNameRequired -> composeStringResource(Res.string.error_spec_name_required)
        AppTextKey.ErrorNoSpecFiles -> composeStringResource(Res.string.error_no_spec_files)
        AppTextKey.ErrorSubspecNameMissing -> composeStringResource(Res.string.error_subspec_name_missing)
        AppTextKey.ErrorOpenSpecRequirementParseFailed ->
            composeStringResource(Res.string.error_openspec_requirement_parse_failed, args.firstOrEmpty())
        AppTextKey.ErrorSpecKitOrRequirementParseFailed ->
            composeStringResource(Res.string.error_spec_kit_or_requirement_parse_failed, args.firstOrEmpty())
        AppTextKey.ErrorTokenRequired -> composeStringResource(Res.string.error_token_required)
        AppTextKey.ErrorBranchRequired -> composeStringResource(Res.string.error_branch_required)
        AppTextKey.ErrorGitHubUnexpectedDirectoryResponse ->
            composeStringResource(Res.string.error_github_unexpected_directory_response, args.firstOrEmpty())
        AppTextKey.ErrorGitHubTokenOrAccess -> composeStringResource(Res.string.error_github_token_or_access)
        AppTextKey.ErrorGitHubNotFound -> composeStringResource(Res.string.error_github_not_found)
        AppTextKey.ErrorGitHubApi -> composeStringResource(Res.string.error_github_api, args.firstOrEmpty())
        AppTextKey.ErrorGitHubRepositoryRequired -> composeStringResource(Res.string.error_github_repository_required)
        AppTextKey.ErrorGitHubOnlySupported -> composeStringResource(Res.string.error_github_only_supported)
        AppTextKey.ErrorGitHubRepositoryFormatRequired -> composeStringResource(Res.string.error_github_repository_format_required)
        AppTextKey.ErrorSpecificationPathRequired -> composeStringResource(Res.string.error_specification_path_required)
        AppTextKey.ErrorAddSpecFailed -> composeStringResource(Res.string.error_add_spec_failed)
        AppTextKey.ErrorRepositoriesLoadFailed -> composeStringResource(Res.string.error_repositories_load_failed)
        AppTextKey.ErrorBranchesLoadFailed -> composeStringResource(Res.string.error_branches_load_failed)
        AppTextKey.ErrorImportUnsupported -> composeStringResource(Res.string.error_import_unsupported)
        AppTextKey.ErrorInvalidTokenData -> composeStringResource(Res.string.error_invalid_token_data)
    }

private fun List<String>.firstOrEmpty(): String = firstOrNull().orEmpty()
