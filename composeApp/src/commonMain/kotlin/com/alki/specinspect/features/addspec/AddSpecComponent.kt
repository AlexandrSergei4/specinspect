package com.alki.specinspect.features.addspec

import com.alki.specinspect.data.importer.AvailableGitRepository
import com.alki.specinspect.data.importer.GitSpecificationImportRequest
import com.alki.specinspect.data.importer.GitSpecificationImporter
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.specification.ImportedSpecificationFactory
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.UserAccessTokenSecureStorage
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
    fun onRepositoryUrlChanged(value: String)
    fun onBranchChanged(value: String)
    fun onSpecificationPathChanged(value: String)
    fun onUserAccessTokenChanged(value: String)
    fun onSubmit()
}

data class RepositoryOption(
    val fullName: String,
    val defaultBranch: String,
)

data class AddSpecState(
    val name: String = "",
    val userAccessToken: String = "",
    val repositories: List<RepositoryOption> = emptyList(),
    val repositoryUrl: String = "",
    val isRepositoriesLoading: Boolean = false,
    val repositoriesStatusMessage: String? = REPOSITORIES_TOKEN_HINT,
    val repositoriesStatusIsError: Boolean = false,
    val branches: List<String> = emptyList(),
    val branch: String = "",
    val isBranchesLoading: Boolean = false,
    val branchesStatusMessage: String? = BRANCHES_REPOSITORY_HINT,
    val branchesStatusIsError: Boolean = false,
    val specificationPath: String = "openspec/specs",
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val errorMessage: String? = null,
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
            repositoriesStatusMessage = if (savedToken.isBlank()) REPOSITORIES_TOKEN_HINT else REPOSITORIES_LOADING_HINT,
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

    override fun onRepositoryUrlChanged(value: String) {
        val current = _state.value
        branchesJob?.cancel()
        _state.value = current.copy(
            name = value.toSpecificationName(),
            repositoryUrl = value,
            branches = emptyList(),
            branch = "",
            isBranchesLoading = value.isNotBlank(),
            branchesStatusMessage = if (value.isBlank()) BRANCHES_REPOSITORY_HINT else BRANCHES_LOADING_HINT,
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
        _state.value = _state.value.copy(
            name = "",
            userAccessToken = value,
            repositories = emptyList(),
            repositoryUrl = "",
            isRepositoriesLoading = value.isNotBlank(),
            repositoriesStatusMessage = if (value.isBlank()) REPOSITORIES_TOKEN_HINT else REPOSITORIES_LOADING_HINT,
            repositoriesStatusIsError = false,
            branches = emptyList(),
            branch = "",
            isBranchesLoading = false,
            branchesStatusMessage = BRANCHES_REPOSITORY_HINT,
            branchesStatusIsError = false,
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
                        errorMessage = e.message ?: "Не удалось добавить спецификацию",
                    )
                    .recomputeCanSubmit(clearError = false)
            }
        }
    }

    private fun loadRepositories() {
        val token = _state.value.userAccessToken.trim()
        if (token.isEmpty()) {
            _state.value = _state.value.copy(
                isRepositoriesLoading = false,
                repositoriesStatusMessage = REPOSITORIES_TOKEN_HINT,
                repositoriesStatusIsError = false,
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
                    repositoriesStatusMessage = if (repositories.isEmpty()) REPOSITORIES_EMPTY_HINT else null,
                    repositoriesStatusIsError = false,
                ).recomputeCanSubmit(clearError = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    repositories = emptyList(),
                    repositoryUrl = "",
                    isRepositoriesLoading = false,
                    repositoriesStatusMessage = e.message ?: "Не удалось загрузить репозитории",
                    repositoriesStatusIsError = true,
                    branchesStatusMessage = BRANCHES_REPOSITORY_HINT,
                    branchesStatusIsError = false,
                ).recomputeCanSubmit(clearError = false)
            }
        }
    }

    private fun loadBranches() {
        val state = _state.value
        val token = state.userAccessToken.trim()
        val repository = state.repositoryUrl.trim()
        if (token.isEmpty() || repository.isEmpty()) {
            _state.value = _state.value.copy(
                isBranchesLoading = false,
                branchesStatusMessage = BRANCHES_REPOSITORY_HINT,
                branchesStatusIsError = false,
            ).recomputeCanSubmit()
            return
        }

        _state.value = _state.value.copy(
            isBranchesLoading = true,
            branchesStatusMessage = BRANCHES_LOADING_HINT,
            branchesStatusIsError = false,
        ).recomputeCanSubmit()
        val defaultBranch = state.repositories.firstOrNull { it.fullName == repository }?.defaultBranch
        branchesJob = scope.launch {
            try {
                val branches = importer.loadBranches(repository, token)
                val sortedBranches = branches.prioritize(defaultBranch)
                _state.value = _state.value.copy(
                    branches = sortedBranches,
                    branch = sortedBranches.firstOrNull().orEmpty(),
                    isBranchesLoading = false,
                    branchesStatusMessage = if (sortedBranches.isEmpty()) BRANCHES_EMPTY_HINT else null,
                    branchesStatusIsError = false,
                ).recomputeCanSubmit(clearError = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    branches = emptyList(),
                    branch = "",
                    isBranchesLoading = false,
                    branchesStatusMessage = e.message ?: "Не удалось загрузить ветки",
                    branchesStatusIsError = true,
                ).recomputeCanSubmit(clearError = false)
            }
        }
    }

    private suspend fun loadSpecification(state: AddSpecState): Specification {
        val files = importer.importSpecification(
            GitSpecificationImportRequest(
                repositoryUrl = state.repositoryUrl.trim(),
                branch = state.branch.trim(),
                specificationPath = state.specificationPath.trim(),
                userAccessToken = state.userAccessToken.trim(),
            )
        )
        return ImportedSpecificationFactory.create(name = state.name, files = files)
    }

    private fun AddSpecState.recomputeCanSubmit(clearError: Boolean = true): AddSpecState =
        copy(
            canSubmit = !isLoading &&
                name.isNotBlank() &&
                userAccessToken.isNotBlank() &&
                repositoryUrl.isNotBlank() &&
                branch.isNotBlank() &&
                specificationPath.isNotBlank() &&
                !isRepositoriesLoading &&
                !isBranchesLoading,
            errorMessage = if (clearError) null else errorMessage,
        )

    private fun AvailableGitRepository.toOption(): RepositoryOption = RepositoryOption(
        fullName = fullName,
        defaultBranch = defaultBranch,
    )

    private fun List<String>.prioritize(preferred: String?): List<String> {
        if (preferred.isNullOrBlank()) return this
        val preferredBranch = firstOrNull { it == preferred } ?: return this
        return listOf(preferredBranch) + filterNot { it == preferredBranch }
    }

    private fun String.toSpecificationName(): String = trim().substringAfterLast('/').removeSuffix(".git")
}

private const val GITHUB_TOKEN_URL = "https://github.com/settings/personal-access-tokens/new?name=SpecInspect+import+token&description=Read+repository+list%2C+branches%2C+and+spec+files+for+SpecInspect&expires_in=30&contents=read"
private const val REPOSITORIES_TOKEN_HINT = "Введите user access token, чтобы загрузить репозитории"
private const val REPOSITORIES_LOADING_HINT = "Загружаем список репозиториев..."
private const val REPOSITORIES_EMPTY_HINT = "У токена нет доступных репозиториев"
private const val BRANCHES_REPOSITORY_HINT = "Выберите репозиторий, чтобы загрузить ветки"
private const val BRANCHES_LOADING_HINT = "Загружаем список веток..."
private const val BRANCHES_EMPTY_HINT = "В репозитории не найдено веток"
