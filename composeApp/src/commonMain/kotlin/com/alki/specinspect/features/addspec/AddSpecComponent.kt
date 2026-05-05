package com.alki.specinspect.features.addspec

import com.alki.specinspect.data.importer.AvailableGitRepository
import com.alki.specinspect.data.importer.GitSpecificationImportRequest
import com.alki.specinspect.data.importer.GitSpecificationImporter
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.specification.ImportedSpecificationFactory
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.UserAccessTokenSecureStorage
import com.alki.specinspect.localization.AppText
import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.appText
import com.alki.specinspect.localization.toAppText
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AddSpecComponent {
    val state: StateFlow<AddSpecState>
    fun onBack()
    fun onNameChanged(value: String)
    fun onGenerateToken()
    fun onRepositorySourceChanged(value: AddSpecRepositorySource)
    fun onRepositoryUrlChanged(value: String)
    fun onPublicRepositoryUrlChanged(value: String)
    fun onBranchChanged(value: String)
    fun onSpecificationPathChanged(value: String)
    fun onUserAccessTokenChanged(value: String)
    fun onSubmit()
}

enum class AddSpecRepositorySource {
    Personal,
    Public,
}

data class RepositoryOption(
    val fullName: String,
    val defaultBranch: String,
)

data class AddSpecState(
    val repositorySource: AddSpecRepositorySource = AddSpecRepositorySource.Personal,
    val name: String = "",
    val userAccessToken: String = "",
    val repositories: List<RepositoryOption> = emptyList(),
    val repositoryUrl: String = "",
    val publicRepositoryUrl: String = "",
    val isRepositoriesLoading: Boolean = false,
    val repositoriesStatusMessage: AppText? = appText(AppTextKey.AddSpecStatusTokenHint),
    val repositoriesStatusIsError: Boolean = false,
    val branches: List<String> = emptyList(),
    val branch: String = "",
    val isBranchesLoading: Boolean = false,
    val branchesStatusMessage: AppText? = appText(AppTextKey.AddSpecStatusSelectRepository),
    val branchesStatusIsError: Boolean = false,
    val specificationPath: String = "openspec/specs",
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val errorMessage: AppText? = null,
)

class DefaultAddSpecComponent(
    componentContext: ComponentContext,
    private val repo: SpecificationRepository,
    private val importer: GitSpecificationImporter,
    private val tokenStorage: UserAccessTokenSecureStorage,
    private val onBackCallback: () -> Unit,
    private val onAddedCallback: () -> Unit,
) : AddSpecComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var repositoriesJob: Job? = null
    private var branchesJob: Job? = null
    private val savedToken = runCatching { tokenStorage.getToken().orEmpty() }.getOrDefault("")
    private val _state = MutableStateFlow(
        AddSpecState(
            userAccessToken = savedToken,
            isRepositoriesLoading = savedToken.isNotBlank(),
            repositoriesStatusMessage = if (savedToken.isBlank()) {
                appText(AppTextKey.AddSpecStatusTokenHint)
            } else {
                appText(AppTextKey.AddSpecStatusRepositoriesLoading)
            },
        )
    )
    override val state: StateFlow<AddSpecState> = _state.asStateFlow()

    init {
        if (savedToken.isNotBlank()) {
            loadRepositories()
        }
    }

    override fun onBack() = onBackCallback()
    override fun onGenerateToken() = UrlOpener.openUrl(GITHUB_TOKEN_URL)

    override fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value).recomputeCanSubmit()
    }

    override fun onRepositorySourceChanged(value: AddSpecRepositorySource) {
        val current = _state.value
        if (current.repositorySource == value) return

        branchesJob?.cancel()
        val next = current.copy(repositorySource = value)
        val repository = next.activeRepositoryUrl().trim()
        _state.value = next.copy(
            name = repository.toSpecificationName(),
            branches = emptyList(),
            branch = "",
            isBranchesLoading = repository.isNotBlank() && next.canLoadActiveBranches(),
            branchesStatusMessage = if (repository.isBlank()) {
                appText(AppTextKey.AddSpecStatusSelectRepository)
            } else if (next.canLoadActiveBranches()) {
                appText(AppTextKey.AddSpecStatusBranchesLoading)
            } else {
                appText(AppTextKey.AddSpecStatusSelectRepository)
            },
            branchesStatusIsError = false,
        ).recomputeCanSubmit()
        loadBranches()
    }

    override fun onRepositoryUrlChanged(value: String) {
        val current = _state.value
        if (current.repositorySource != AddSpecRepositorySource.Personal) {
            _state.value = current.copy(repositoryUrl = value).recomputeCanSubmit()
            return
        }

        branchesJob?.cancel()
        _state.value = current.copy(
            name = value.toSpecificationName(),
            repositoryUrl = value,
            branches = emptyList(),
            branch = "",
            isBranchesLoading = value.isNotBlank(),
            branchesStatusMessage = if (value.isBlank()) {
                appText(AppTextKey.AddSpecStatusSelectRepository)
            } else {
                appText(AppTextKey.AddSpecStatusBranchesLoading)
            },
            branchesStatusIsError = false,
        ).recomputeCanSubmit()
        loadBranches()
    }

    override fun onPublicRepositoryUrlChanged(value: String) {
        val current = _state.value
        if (current.repositorySource != AddSpecRepositorySource.Public) {
            _state.value = current.copy(publicRepositoryUrl = value).recomputeCanSubmit()
            return
        }

        branchesJob?.cancel()
        _state.value = current.copy(
            name = value.toSpecificationName(),
            publicRepositoryUrl = value,
            branches = emptyList(),
            branch = "",
            isBranchesLoading = value.isNotBlank(),
            branchesStatusMessage = if (value.isBlank()) {
                appText(AppTextKey.AddSpecStatusSelectRepository)
            } else {
                appText(AppTextKey.AddSpecStatusBranchesLoading)
            },
            branchesStatusIsError = false,
        ).recomputeCanSubmit()
        loadBranches()
    }

    override fun onBranchChanged(value: String) {
        _state.value = _state.value.copy(branch = value).recomputeCanSubmit()
    }

    override fun onSpecificationPathChanged(value: String) {
        _state.value = _state.value.copy(specificationPath = value).recomputeCanSubmit()
    }

    override fun onUserAccessTokenChanged(value: String) {
        persistToken(value)
        repositoriesJob?.cancel()
        branchesJob?.cancel()
        val current = _state.value
        val isPersonalSource = current.repositorySource == AddSpecRepositorySource.Personal
        _state.value = current.copy(
            name = if (isPersonalSource) "" else current.name,
            userAccessToken = value,
            repositories = emptyList(),
            repositoryUrl = if (isPersonalSource) "" else current.repositoryUrl,
            isRepositoriesLoading = value.isNotBlank(),
            repositoriesStatusMessage = if (value.isBlank()) {
                appText(AppTextKey.AddSpecStatusTokenHint)
            } else {
                appText(AppTextKey.AddSpecStatusRepositoriesLoading)
            },
            repositoriesStatusIsError = false,
            branches = if (isPersonalSource) emptyList() else current.branches,
            branch = if (isPersonalSource) "" else current.branch,
            isBranchesLoading = if (isPersonalSource) false else current.isBranchesLoading,
            branchesStatusMessage = if (isPersonalSource) {
                appText(AppTextKey.AddSpecStatusSelectRepository)
            } else {
                current.branchesStatusMessage
            },
            branchesStatusIsError = if (isPersonalSource) false else current.branchesStatusIsError,
        ).recomputeCanSubmit()
        loadRepositories()
    }

    private fun persistToken(value: String) {
        val normalizedValue = value.trim()
        runCatching {
            if (normalizedValue.isBlank()) tokenStorage.clearToken() else tokenStorage.saveToken(normalizedValue)
        }
    }

    override fun onSubmit() {
        val s = _state.value
        if (!s.canSubmit || s.isLoading) return

        _state.value = s.copy(isLoading = true).recomputeCanSubmit()
        scope.launch {
            try {
                val spec = loadSpecification(s)
                withContext(Dispatchers.Main) {
                    repo.add(spec)
                    onAddedCallback()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value
                    .copy(
                        isLoading = false,
                        errorMessage = e.toAppText(AppTextKey.ErrorAddSpecFailed),
                    )
                    .recomputeCanSubmit(clearError = false)
            }
        }
    }

    private fun loadRepositories() {
        val token = _state.value.userAccessToken.trim()
        if (token.isEmpty()) {
            val current = _state.value
            val isPersonalSource = current.repositorySource == AddSpecRepositorySource.Personal
            _state.value = current.copy(
                isRepositoriesLoading = false,
                repositoriesStatusMessage = appText(AppTextKey.AddSpecStatusTokenHint),
                repositoriesStatusIsError = false,
                isBranchesLoading = if (isPersonalSource) false else current.isBranchesLoading,
                branchesStatusMessage = if (isPersonalSource) {
                    appText(AppTextKey.AddSpecStatusSelectRepository)
                } else {
                    current.branchesStatusMessage
                },
                branchesStatusIsError = if (isPersonalSource) false else current.branchesStatusIsError,
            ).recomputeCanSubmit()
            return
        }

        repositoriesJob = scope.launch {
            try {
                delay(400)
                val repositories = importer.loadRepositories(token).map { it.toOption() }
                _state.value = _state.value.copy(
                    repositories = repositories,
                    isRepositoriesLoading = false,
                    repositoriesStatusMessage = if (repositories.isEmpty()) {
                        appText(AppTextKey.AddSpecStatusRepositoriesEmpty)
                    } else {
                        null
                    },
                    repositoriesStatusIsError = false,
                ).recomputeCanSubmit(clearError = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val current = _state.value
                val isPersonalSource = current.repositorySource == AddSpecRepositorySource.Personal
                _state.value = current.copy(
                    repositories = emptyList(),
                    repositoryUrl = "",
                    isRepositoriesLoading = false,
                    repositoriesStatusMessage = e.toAppText(AppTextKey.ErrorRepositoriesLoadFailed),
                    repositoriesStatusIsError = true,
                    branches = if (isPersonalSource) emptyList() else current.branches,
                    branch = if (isPersonalSource) "" else current.branch,
                    isBranchesLoading = if (isPersonalSource) false else current.isBranchesLoading,
                    branchesStatusMessage = if (isPersonalSource) {
                        appText(AppTextKey.AddSpecStatusSelectRepository)
                    } else {
                        current.branchesStatusMessage
                    },
                    branchesStatusIsError = if (isPersonalSource) false else current.branchesStatusIsError,
                ).recomputeCanSubmit(clearError = false)
            }
        }
    }

    private fun loadBranches() {
        val state = _state.value
        val source = state.repositorySource
        val token = state.activeRepositoryToken()
        val repository = state.activeRepositoryUrl().trim()
        if (repository.isEmpty() || !state.canLoadActiveBranches()) {
            _state.value = _state.value.copy(
                isBranchesLoading = false,
                branchesStatusMessage = appText(AppTextKey.AddSpecStatusSelectRepository),
                branchesStatusIsError = false,
            ).recomputeCanSubmit()
            return
        }

        _state.value = _state.value.copy(
            isBranchesLoading = true,
            branchesStatusMessage = appText(AppTextKey.AddSpecStatusBranchesLoading),
            branchesStatusIsError = false,
        ).recomputeCanSubmit()
        val defaultBranch = if (source == AddSpecRepositorySource.Personal) {
            state.repositories.firstOrNull { it.fullName == repository }?.defaultBranch
        } else {
            null
        }
        branchesJob = scope.launch {
            try {
                val repositoryInfo = if (source == AddSpecRepositorySource.Public || defaultBranch.isNullOrBlank()) {
                    importer.loadRepository(repository, token)
                } else {
                    null
                }
                val effectiveDefaultBranch = defaultBranch ?: repositoryInfo?.defaultBranch
                val publicSelection = if (source == AddSpecRepositorySource.Public) {
                    repository.publicRepositorySelection(effectiveDefaultBranch)
                } else {
                    PublicRepositorySelection()
                }
                val branches = importer.loadBranches(repository, token)
                val preferredBranch = publicSelection.branch ?: effectiveDefaultBranch
                val sortedBranches = branches.prioritize(preferredBranch)
                val selectedBranch = sortedBranches.firstOrNull { it == publicSelection.branch }
                    ?: sortedBranches.firstOrNull().orEmpty()
                val current = _state.value
                if (!current.isCurrentBranchRequest(source, repository)) return@launch
                _state.value = current.copy(
                    branches = sortedBranches,
                    branch = selectedBranch,
                    specificationPath = publicSelection.specificationPath
                        ?.takeIf { current.specificationPath == state.specificationPath }
                        ?: current.specificationPath,
                    isBranchesLoading = false,
                    branchesStatusMessage = if (sortedBranches.isEmpty()) {
                        appText(AppTextKey.AddSpecStatusBranchesEmpty)
                    } else {
                        null
                    },
                    branchesStatusIsError = false,
                ).recomputeCanSubmit(clearError = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val current = _state.value
                if (!current.isCurrentBranchRequest(source, repository)) return@launch
                _state.value = current.copy(
                    branches = emptyList(),
                    branch = "",
                    isBranchesLoading = false,
                    branchesStatusMessage = e.toAppText(AppTextKey.ErrorBranchesLoadFailed),
                    branchesStatusIsError = true,
                ).recomputeCanSubmit(clearError = false)
            }
        }
    }

    private suspend fun loadSpecification(state: AddSpecState): Specification {
        val repositoryUrl = state.activeRepositoryUrl().trim()
        val branch = state.branch.trim()
        val files = importer.importSpecification(
            GitSpecificationImportRequest(
                repositoryUrl = repositoryUrl,
                branch = branch,
                specificationPath = state.specificationPath.trim(),
                userAccessToken = state.activeRepositoryToken(),
            )
        )
        return ImportedSpecificationFactory.create(
            name = state.name,
            files = files,
            gitSource = ImportedSpecificationFactory.gitSourceFrom(
                repositoryUrl = repositoryUrl,
                branch = branch,
            ),
        )
    }

    private fun AddSpecState.recomputeCanSubmit(clearError: Boolean = true): AddSpecState =
        copy(
            canSubmit = !isLoading &&
                name.isNotBlank() &&
                (repositorySource == AddSpecRepositorySource.Public || userAccessToken.isNotBlank()) &&
                activeRepositoryUrl().isNotBlank() &&
                branch.isNotBlank() &&
                specificationPath.isNotBlank() &&
                (repositorySource == AddSpecRepositorySource.Public || !isRepositoriesLoading) &&
                !isBranchesLoading,
            errorMessage = if (clearError) null else errorMessage,
        )

    private fun AddSpecState.activeRepositoryUrl(): String =
        when (repositorySource) {
            AddSpecRepositorySource.Personal -> repositoryUrl
            AddSpecRepositorySource.Public -> publicRepositoryUrl
        }

    private fun AddSpecState.activeRepositoryToken(): String =
        when (repositorySource) {
            AddSpecRepositorySource.Personal -> userAccessToken.trim()
            AddSpecRepositorySource.Public -> ""
        }

    private fun AddSpecState.canLoadActiveBranches(): Boolean =
        activeRepositoryUrl().isNotBlank() &&
            (repositorySource == AddSpecRepositorySource.Public || userAccessToken.isNotBlank())

    private fun AddSpecState.isCurrentBranchRequest(
        source: AddSpecRepositorySource,
        repository: String,
    ): Boolean =
        repositorySource == source && activeRepositoryUrl().trim() == repository

    private fun AvailableGitRepository.toOption(): RepositoryOption = RepositoryOption(
        fullName = fullName,
        defaultBranch = defaultBranch,
    )

    private fun List<String>.prioritize(preferred: String?): List<String> {
        if (preferred.isNullOrBlank()) return this
        val preferredBranch = firstOrNull { it == preferred } ?: return this
        return listOf(preferredBranch) + filterNot { it == preferredBranch }
    }

    private fun String.toSpecificationName(): String {
        val parts = githubPathParts()
        return if (parts.size >= 2) {
            parts[1].removeSuffix(".git")
        } else {
            trim().trimEnd('/').substringAfterLast('/').removeSuffix(".git")
        }
    }

    private fun String.publicRepositorySelection(defaultBranch: String?): PublicRepositorySelection {
        val parts = githubPathParts()
        val markerIndex = parts.indexOfFirst { it == "tree" || it == "blob" }
        if (parts.size < 2 || markerIndex < 2 || markerIndex == parts.lastIndex) {
            return PublicRepositorySelection()
        }

        val remainder = parts.drop(markerIndex + 1)
        if (remainder.isEmpty()) return PublicRepositorySelection()

        val defaultBranchParts = defaultBranch
            ?.takeIf { it.isNotBlank() }
            ?.split('/')
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val branchParts = if (
            defaultBranchParts.isNotEmpty() &&
            remainder.size >= defaultBranchParts.size &&
            remainder.take(defaultBranchParts.size) == defaultBranchParts
        ) {
            defaultBranchParts
        } else {
            listOf(remainder.first())
        }
        val path = remainder.drop(branchParts.size).joinToString("/")

        return PublicRepositorySelection(
            branch = branchParts.joinToString("/"),
            specificationPath = path.takeIf { it.isNotBlank() },
        )
    }

    private fun String.githubPathParts(): List<String> {
        val value = trim()
            .substringBefore('?')
            .substringBefore('#')
            .removePrefix("git@github.com:")
            .removePrefix("ssh://git@github.com/")
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("github.com/")
            .trim('/')

        return value.split('/').filter { it.isNotBlank() }
    }
}

private data class PublicRepositorySelection(
    val branch: String? = null,
    val specificationPath: String? = null,
)

private const val GITHUB_TOKEN_URL = "https://github.com/settings/personal-access-tokens/new?name=SpecInspect+import+token&description=Read+repository+list%2C+branches%2C+and+spec+files+for+SpecInspect&expires_in=30&contents=read"
