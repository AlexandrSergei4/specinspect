package com.alki.specinspect.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.alki.specinspect.features.review.ImportedSpecLibrary
import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.PersistedReviewState
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.ReviewDecision
import com.alki.specinspect.features.review.ReviewEngine
import com.alki.specinspect.features.review.ReviewMode
import com.alki.specinspect.features.review.ReviewStatus
import com.alki.specinspect.features.review.ScenarioScope
import com.alki.specinspect.features.review.ScopeReviewStats
import com.alki.specinspect.features.review.data.ReviewStateStore
import com.alki.specinspect.features.review.platform.PlatformFolderImporter
import com.alki.specinspect.features.review.ui.loadDemoLibrary
import com.alki.specinspect.ui.theme.SampleTheme
import com.alki.specinspect.ui.theme.SpecInspectColors
import com.arkivanov.decompose.extensions.compose.stack.Children
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    val store = remember { ReviewStateStore() }
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<PersistedReviewState?>(null) }
    var addDraftName by remember { mutableStateOf("") }
    var addDraftRaw by remember { mutableStateOf<RawImportedLibrary?>(null) }
    var addError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val restored = store.load() ?: PersistedReviewState()
        val withDemo = ensureDemoLibraryExists(store, restored)
        state = withDemo
        if (!withDemo.showOnboardingOnLaunch) {
            component.openLibrariesRoot()
        }
    }

    LaunchedEffect(state) {
        state?.let { store.save(it) }
    }

    setSingletonImageLoaderFactory { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    SampleTheme {
        val currentState = state
        if (currentState == null) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@SampleTheme
        }

        Children(stack = component.stack, modifier = modifier.fillMaxSize()) { child ->
            when (val instance = child.instance) {
                RootComponent.Child.Onboarding -> OnboardingScreen(
                    showOnStartup = currentState.showOnboardingOnLaunch,
                    onShowOnStartupChange = { enabled ->
                        state = currentState.copy(showOnboardingOnLaunch = enabled)
                    },
                    onStartDemo = {
                        scope.launch {
                            val withDemo = ensureDemoLibraryExists(store, currentState)
                            val demo = withDemo.libraries.first { it.sourceType == LibrarySourceType.Demo }
                            state = withDemo.copy(
                                activeLibrary = demo,
                                currentMode = ReviewMode.Unreviewed
                            )
                            component.openReview(
                                ReviewScope.Specification(demo.specifications.firstOrNull()?.specId.orEmpty())
                            )
                        }
                    },
                    onOpenLibrary = {
                        component.openLibrariesRoot()
                    }
                )

                RootComponent.Child.MySpecifications -> MySpecificationsScreen(
                    libraries = visibleLibraries(currentState),
                    onAdd = { component.openAddSpecification() },
                    onOpenSpecification = { library ->
                        state = currentState.copy(activeLibrary = library)
                        val spec = library.specifications.firstOrNull()
                        if (spec != null) {
                            component.openSpecification(spec.specId)
                        }
                    },
                    onStartReview = { library ->
                        state = currentState.copy(activeLibrary = library, currentMode = ReviewMode.Unreviewed)
                        component.openReview(
                            ReviewScope.Specification(library.specifications.firstOrNull()?.specId.orEmpty())
                        )
                    },
                    onDelete = { library ->
                        state = if (library.sourceType == LibrarySourceType.Demo) {
                            currentState.copy(isDemoHiddenInLibrary = true)
                        } else {
                            val updated = currentState.libraries.filterNot { it.libraryKey() == library.libraryKey() }
                            currentState.copy(
                                libraries = updated,
                                activeLibrary = if (currentState.activeLibrary?.libraryKey() == library.libraryKey()) null else currentState.activeLibrary
                            )
                        }
                    }
                )

                RootComponent.Child.AddSpecification -> AddSpecificationScreen(
                    name = addDraftName,
                    selectedFolderName = addDraftRaw?.sourceName,
                    error = addError,
                    canPickFolder = PlatformFolderImporter.isSupported,
                    onNameChange = { addDraftName = it },
                    onPickFolder = {
                        scope.launch {
                            addError = null
                            val imported = PlatformFolderImporter.pickLibrary()
                            if (imported == null) {
                                addError = "Папка не выбрана или не содержит валидную структуру OpenSpec."
                            } else {
                                addDraftRaw = imported
                                if (addDraftName.isBlank()) {
                                    addDraftName = imported.sourceName
                                }
                            }
                        }
                    },
                    onBack = { component.goBack() },
                    onSave = {
                        val raw = addDraftRaw ?: return@AddSpecificationScreen
                        val trimmed = addDraftName.trim()
                        if (trimmed.isBlank()) {
                            addError = "Укажите название спецификации."
                            return@AddSpecificationScreen
                        }
                        scope.launch {
                            val renamed = raw.copy(
                                sourceName = trimmed,
                                sourceType = LibrarySourceType.Custom
                            )
                            state = store.importLibrary(currentState, renamed)
                            addDraftName = ""
                            addDraftRaw = null
                            addError = null
                            component.openLibrariesRoot()
                        }
                    }
                )

                is RootComponent.Child.SpecificationDetail ->
                    SpecificationDetailScreen(
                        state = currentState,
                        specId = instance.specId,
                        onBack = { component.goBack() },
                        onStartReview = {
                            component.openReview(ReviewScope.Specification(instance.specId))
                        },
                        onOpenSubspec = { subspecId ->
                            component.openSubspec(instance.specId, subspecId)
                        }
                    )
                is RootComponent.Child.SubspecDetail ->
                    SubspecDetailScreen(
                        state = currentState,
                        specId = instance.specId,
                        subspecId = instance.subspecId,
                        onBack = { component.goBack() },
                        onStartReview = {
                            component.openReview(ReviewScope.Subspec(instance.specId, instance.subspecId))
                        },
                        onOpenRequirement = { requirementId ->
                            component.openRequirement(instance.specId, instance.subspecId, requirementId)
                        }
                    )
                is RootComponent.Child.RequirementDetail ->
                    RequirementDetailScreen(
                        state = currentState,
                        specId = instance.specId,
                        subspecId = instance.subspecId,
                        requirementId = instance.requirementId,
                        onBack = { component.goBack() },
                        onStartReview = {
                            component.openReview(
                                ReviewScope.Requirement(
                                    instance.specId,
                                    instance.subspecId,
                                    instance.requirementId
                                )
                            )
                        },
                        onScenarioDecision = { scenarioId, decision ->
                            state = currentState.copy(
                                scenarioDecisions = currentState.scenarioDecisions + (scenarioId to decision)
                            )
                        }
                    )
                is RootComponent.Child.ScenarioReview -> ScenarioReviewScreen(
                    state = currentState,
                    scope = instance.scope,
                    onBack = { component.goBack() },
                    onDecision = { scenarioId, decision ->
                        state = currentState.copy(
                            scenarioDecisions = currentState.scenarioDecisions + (scenarioId to decision)
                        )
                    }
                )
            }
        }
    }
}

private suspend fun ensureDemoLibraryExists(
    store: ReviewStateStore,
    state: PersistedReviewState
): PersistedReviewState {
    val hasDemo = state.libraries.any { it.sourceType == LibrarySourceType.Demo }
    if (hasDemo) return state

    val imported = store.importLibrary(state, loadDemoLibrary())
    return imported.copy(
        activeLibrary = state.activeLibrary ?: imported.activeLibrary,
        currentMode = state.currentMode,
        showOnboardingOnLaunch = state.showOnboardingOnLaunch,
        isDemoHiddenInLibrary = state.isDemoHiddenInLibrary,
        scenarioDecisions = state.scenarioDecisions,
        swipeHistory = state.swipeHistory
    )
}

private fun visibleLibraries(state: PersistedReviewState): List<ImportedSpecLibrary> =
    state.libraries.filterNot { library ->
        state.isDemoHiddenInLibrary && library.sourceType == LibrarySourceType.Demo
    }

private fun ImportedSpecLibrary.libraryKey(): String = "$sourceName|$importedAtEpochMillis|${sourceType.name}"

@Composable
private fun OnboardingScreen(
    showOnStartup: Boolean,
    onShowOnStartupChange: (Boolean) -> Unit,
    onStartDemo: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "SpecInspect",
            style = MaterialTheme.typography.headlineMedium,
            color = SpecInspectColors.Ink
        )
        Text(
            text = "Ревью спецификаций по одному сценарию за раз. Отмечайте корректные и некорректные сценарии, чтобы потом исправлять их вне приложения.",
            style = MaterialTheme.typography.bodyMedium,
            color = SpecInspectColors.Muted
        )

        Button(
            onClick = onStartDemo,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpecInspectColors.Ink,
                contentColor = SpecInspectColors.Background
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Начать с демо-спецификацией")
        }

        OutlinedButton(
            onClick = onOpenLibrary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.35f))
        ) {
            Text("Перейти к моим спецификациям")
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Как это работает?", fontWeight = FontWeight.SemiBold, color = SpecInspectColors.Ink)
                Text("• Выберите спецификацию или демо", color = SpecInspectColors.Muted, style = MaterialTheme.typography.bodySmall)
                Text("• Ревьюьте сценарии по одному", color = SpecInspectColors.Muted, style = MaterialTheme.typography.bodySmall)
                Text("• Свайп влево: некорректный, вправо: корректный", color = SpecInspectColors.Muted, style = MaterialTheme.typography.bodySmall)
                Text("• Используйте статистику для анализа проблем", color = SpecInspectColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }

        Surface(
            color = SpecInspectColors.Surface,
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Показывать этот экран при запуске",
                    color = SpecInspectColors.Ink
                )
                Switch(
                    checked = showOnStartup,
                    onCheckedChange = onShowOnStartupChange
                )
            }
        }
    }
}

@Composable
private fun MySpecificationsScreen(
    libraries: List<ImportedSpecLibrary>,
    onAdd: () -> Unit,
    onOpenSpecification: (ImportedSpecLibrary) -> Unit,
    onStartReview: (ImportedSpecLibrary) -> Unit,
    onDelete: (ImportedSpecLibrary) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Мои спецификации",
            style = MaterialTheme.typography.headlineMedium,
            color = SpecInspectColors.Ink
        )

        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.35f))
        ) {
            Text("+ Добавить спецификации")
        }

        Text(
            text = "Загруженные спецификации",
            style = MaterialTheme.typography.labelLarge,
            color = SpecInspectColors.Muted
        )

        if (libraries.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
                border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "Список пока пуст. Добавьте OpenSpec библиотеку или запустите демо из онбординга.",
                    modifier = Modifier.padding(16.dp),
                    color = SpecInspectColors.Muted
                )
            }
        } else {
            libraries.forEach { library ->
                val requirements = library.specifications.flatMap { it.requirements }.size
                val scenarios = library.specifications.flatMap { it.scenarios }.size
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
                    border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(library.sourceName, fontWeight = FontWeight.SemiBold, color = SpecInspectColors.Ink)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (library.sourceType == LibrarySourceType.Demo) "Демо" else "Custom",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpecInspectColors.Muted
                                )
                                IconButton(onClick = { onDelete(library) }) {
                                    Text("Del", color = SpecInspectColors.Incorrect)
                                }
                            }
                        }
                        Text(
                            text = "${library.specifications.size} subspec • $requirements requirements • $scenarios scenarios",
                            color = SpecInspectColors.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = { onOpenSpecification(library) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpecInspectColors.Correct,
                                contentColor = SpecInspectColors.Background
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Открыть")
                        }
                        Button(
                            onClick = { onStartReview(library) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpecInspectColors.Ink,
                                contentColor = SpecInspectColors.Background
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Начать ревью")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSpecificationScreen(
    name: String,
    selectedFolderName: String?,
    error: String?,
    canPickFolder: Boolean,
    onNameChange: (String) -> Unit,
    onPickFolder: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("←", color = SpecInspectColors.Ink)
            }
            Text(
                text = "Добавить спецификацию",
                style = MaterialTheme.typography.headlineSmall,
                color = SpecInspectColors.Ink
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Название спецификации", color = SpecInspectColors.Ink)
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("Например: Проект X") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedButton(
                    onClick = onPickFolder,
                    enabled = canPickFolder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (selectedFolderName == null) "Выбрать папку со спецификациями" else "Папка: $selectedFolderName")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedFolderName != null && name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpecInspectColors.Ink,
                        contentColor = SpecInspectColors.Background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Добавить")
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = SpecInspectColors.Incorrect,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Формат спецификаций", fontWeight = FontWeight.SemiBold, color = SpecInspectColors.Ink)
                Text(
                    text = "Выберите папку `specs` (или корень проекта, в котором есть `specs`). Каждая subspec должна лежать в отдельной папке и содержать `spec.md`.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpecInspectColors.Muted
                )
                HorizontalDivider()
                Text(
                    text = "Пример:\nspecs/\n├─ dashboard/spec.md\n└─ events-feed/spec.md",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpecInspectColors.Ink
                )
            }
        }
    }
}

@Composable
private fun SpecificationDetailScreen(
    state: PersistedReviewState,
    specId: String,
    onBack: () -> Unit,
    onStartReview: () -> Unit,
    onOpenSubspec: (String) -> Unit
) {
    val library = state.activeLibrary ?: run {
        PlaceholderScreen("No active library")
        return
    }
    val hierarchy = ReviewEngine.hierarchyStats(
        library = library,
        history = state.swipeHistory,
        scenarioDecisions = state.scenarioDecisions
    )
    val spec = hierarchy.firstOrNull { it.specId == specId } ?: run {
        PlaceholderScreen("Specification not found")
        return
    }
    var filter by remember(specId) { mutableStateOf<ReviewStatus?>(null) }
    val filteredSubspecs = spec.subspecs.filter { statsMatch(it.stats, filter) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderWithBack(title = spec.displayName, onBack = onBack)
        StatsSelector(spec.stats, filter = filter, onSelect = { filter = it })
        Button(
            onClick = onStartReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpecInspectColors.Ink,
                contentColor = SpecInspectColors.Background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Начать ревью")
        }
        filteredSubspecs.forEach { subspec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
                border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f)),
                onClick = { onOpenSubspec(subspec.subspecId) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(subspec.displayName, color = SpecInspectColors.Ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${subspec.requirementCount} requirements • ${subspec.stats.totalScenarios} scenarios",
                        color = SpecInspectColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    StatsLine(subspec.stats)
                }
            }
        }
    }
}

@Composable
private fun SubspecDetailScreen(
    state: PersistedReviewState,
    specId: String,
    subspecId: String,
    onBack: () -> Unit,
    onStartReview: () -> Unit,
    onOpenRequirement: (String) -> Unit
) {
    val library = state.activeLibrary ?: run {
        PlaceholderScreen("No active library")
        return
    }
    val hierarchy = ReviewEngine.hierarchyStats(
        library = library,
        history = state.swipeHistory,
        scenarioDecisions = state.scenarioDecisions
    )
    val spec = hierarchy.firstOrNull { it.specId == specId } ?: run {
        PlaceholderScreen("Specification not found")
        return
    }
    val subspec = spec.subspecs.firstOrNull { it.subspecId == subspecId } ?: run {
        PlaceholderScreen("Subspec not found")
        return
    }
    var filter by remember(subspecId) { mutableStateOf<ReviewStatus?>(null) }
    val filteredRequirements = subspec.requirements.filter { statsMatch(it.stats, filter) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderWithBack(title = subspec.displayName, subtitle = spec.displayName, onBack = onBack)
        StatsSelector(subspec.stats, filter = filter, onSelect = { filter = it })
        Button(
            onClick = onStartReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpecInspectColors.Ink,
                contentColor = SpecInspectColors.Background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Начать ревью")
        }

        filteredRequirements.forEach { requirement ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
                border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f)),
                onClick = { onOpenRequirement(requirement.requirementId) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(requirement.requirementTitle, color = SpecInspectColors.Ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${requirement.stats.totalScenarios} scenarios",
                        color = SpecInspectColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    StatsLine(requirement.stats)
                }
            }
        }
    }
}

@Composable
private fun RequirementDetailScreen(
    state: PersistedReviewState,
    specId: String,
    subspecId: String,
    requirementId: String,
    onBack: () -> Unit,
    onStartReview: () -> Unit,
    onScenarioDecision: (scenarioId: String, decision: ReviewDecision) -> Unit
) {
    val library = state.activeLibrary ?: run {
        PlaceholderScreen("No active library")
        return
    }
    val spec = library.specifications.firstOrNull { it.specId == specId } ?: run {
        PlaceholderScreen("Specification not found")
        return
    }
    val subspec = spec.subspecs.firstOrNull { it.subspecId == subspecId } ?: run {
        PlaceholderScreen("Subspec not found")
        return
    }
    val requirement = subspec.requirements.firstOrNull { it.requirementId == requirementId } ?: run {
        PlaceholderScreen("Requirement not found")
        return
    }
    val requirementStats = ReviewEngine.hierarchyStats(
        library = library,
        history = state.swipeHistory,
        scenarioDecisions = state.scenarioDecisions
    ).firstOrNull { it.specId == specId }
        ?.subspecs?.firstOrNull { it.subspecId == subspecId }
        ?.requirements?.firstOrNull { it.requirementId == requirementId }
        ?.stats ?: ScopeReviewStats(
        totalScenarios = requirement.scenarios.size,
        correctScenarios = 0,
        incorrectScenarios = 0,
        unreviewedScenarios = requirement.scenarios.size
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderWithBack(
            title = requirement.requirementTitle,
            subtitle = "${spec.displayName} / ${subspec.displayName}",
            onBack = onBack
        )
        StatsSelector(requirementStats, filter = null, onSelect = {})
        Button(
            onClick = onStartReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpecInspectColors.Ink,
                contentColor = SpecInspectColors.Background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Начать ревью")
        }
        requirement.scenarios.forEach { scenario ->
            val decision = state.scenarioDecisions[scenario.scenarioId]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
                border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(scenario.title, color = SpecInspectColors.Ink, fontWeight = FontWeight.SemiBold)
                    Surface(
                        color = SpecInspectColors.Correct.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SpecInspectColors.Correct.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("WHEN", color = SpecInspectColors.Correct, style = MaterialTheme.typography.labelSmall)
                            Text(scenario.whenText, color = SpecInspectColors.Ink, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Surface(
                        color = SpecInspectColors.AccentThen.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SpecInspectColors.AccentThen.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("THEN", color = SpecInspectColors.Ink, style = MaterialTheme.typography.labelSmall)
                            Text(scenario.thenText, color = SpecInspectColors.Ink, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onScenarioDecision(scenario.scenarioId, ReviewDecision.Rejected) },
                            border = BorderStroke(
                                1.dp,
                                if (decision == ReviewDecision.Rejected) SpecInspectColors.Incorrect else SpecInspectColors.Muted.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("Некорректный")
                        }
                        OutlinedButton(
                            onClick = { onScenarioDecision(scenario.scenarioId, ReviewDecision.Approved) },
                            border = BorderStroke(
                                1.dp,
                                if (decision == ReviewDecision.Approved) SpecInspectColors.Correct else SpecInspectColors.Muted.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("Корректный")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioReviewScreen(
    state: PersistedReviewState,
    scope: ReviewScope,
    onBack: () -> Unit,
    onDecision: (scenarioId: String, decision: ReviewDecision) -> Unit
) {
    val library = state.activeLibrary ?: run {
        PlaceholderScreen("No active library")
        return
    }
    val queue = remember(scope, library, state.scenarioDecisions) {
        ReviewEngine.unreviewedScenarioQueue(
            library = library,
            scope = scope.toScenarioScope(),
            scenarioDecisions = state.scenarioDecisions
        )
            .toMutableList()
    }
    var currentScenarioId by remember(scope, queue) {
        mutableStateOf(queue.randomOrNull()?.scenarioId)
    }
    var dragOffsetX by remember { mutableStateOf(0f) }

    LaunchedEffect(queue.size, currentScenarioId) {
        if (queue.isEmpty() || currentScenarioId == null) {
            onBack()
        }
    }

    val current = currentScenarioId?.let { id -> queue.firstOrNull { it.scenarioId == id } }
    if (current == null) {
        PlaceholderScreen("Завершено")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("<-", color = SpecInspectColors.Ink)
            }
            Text(
                text = "${queue.size} осталось",
                color = SpecInspectColors.Muted
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(current.scenarioId) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                dragOffsetX > 120f -> {
                                    applyScenarioDecision(
                                        current = current,
                                        queue = queue,
                                        onDecision = onDecision,
                                        decision = ReviewDecision.Approved,
                                        onNext = { next -> currentScenarioId = next }
                                    )
                                }
                                dragOffsetX < -120f -> {
                                    applyScenarioDecision(
                                        current = current,
                                        queue = queue,
                                        onDecision = onDecision,
                                        decision = ReviewDecision.Rejected,
                                        onNext = { next -> currentScenarioId = next }
                                    )
                                }
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = { dragOffsetX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                        }
                    )
                },
            colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
            border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = current.title,
                    color = SpecInspectColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = SpecInspectColors.Correct.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SpecInspectColors.Correct.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("WHEN", color = SpecInspectColors.Correct, style = MaterialTheme.typography.labelSmall)
                        Text(current.whenText, color = SpecInspectColors.Ink)
                    }
                }
                Surface(
                    color = SpecInspectColors.AccentThen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SpecInspectColors.AccentThen.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("THEN", color = SpecInspectColors.Ink, style = MaterialTheme.typography.labelSmall)
                        Text(current.thenText, color = SpecInspectColors.Ink)
                    }
                }
                Text(
                    text = "Свайп влево: некорректный, вправо: корректный",
                    color = SpecInspectColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    applyScenarioDecision(
                        current = current,
                        queue = queue,
                        onDecision = onDecision,
                        decision = ReviewDecision.Rejected,
                        onNext = { next -> currentScenarioId = next }
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpecInspectColors.Incorrect,
                    contentColor = SpecInspectColors.Background
                )
            ) {
                Text("Некорректный")
            }
            Button(
                onClick = {
                    applyScenarioDecision(
                        current = current,
                        queue = queue,
                        onDecision = onDecision,
                        decision = ReviewDecision.Approved,
                        onNext = { next -> currentScenarioId = next }
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpecInspectColors.Correct,
                    contentColor = SpecInspectColors.Background
                )
            ) {
                Text("Корректный")
            }
        }
    }
}

private fun applyScenarioDecision(
    current: com.alki.specinspect.features.review.ImportedScenario,
    queue: MutableList<com.alki.specinspect.features.review.ImportedScenario>,
    onDecision: (scenarioId: String, decision: ReviewDecision) -> Unit,
    decision: ReviewDecision,
    onNext: (String?) -> Unit
) {
    onDecision(current.scenarioId, decision)
    queue.removeAll { it.scenarioId == current.scenarioId }
    onNext(queue.randomOrNull()?.scenarioId)
}

private fun ReviewScope.toScenarioScope(): ScenarioScope = when (this) {
    is ReviewScope.Specification -> ScenarioScope.Specification(specId = specId)
    is ReviewScope.Subspec -> ScenarioScope.Subspec(specId = specId, subspecId = subspecId)
    is ReviewScope.Requirement -> ScenarioScope.Requirement(
        specId = specId,
        subspecId = subspecId,
        requirementId = requirementId
    )
}

@Composable
private fun HeaderWithBack(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Text("<-", color = SpecInspectColors.Ink)
        }
        Column {
            if (subtitle != null) {
                Text(subtitle, color = SpecInspectColors.Muted, style = MaterialTheme.typography.labelSmall)
            }
            Text(title, color = SpecInspectColors.Ink, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatsSelector(
    stats: ScopeReviewStats,
    filter: ReviewStatus?,
    onSelect: (ReviewStatus?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpecInspectColors.Surface),
        border = BorderStroke(1.dp, SpecInspectColors.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsLine(stats)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("Все", filter == null) { onSelect(null) }
                StatusChip("Корректные", filter == ReviewStatus.Correct) { onSelect(ReviewStatus.Correct) }
                StatusChip("Некорректные", filter == ReviewStatus.Incorrect) { onSelect(ReviewStatus.Incorrect) }
                StatusChip("Неоцененные", filter == ReviewStatus.Unreviewed) { onSelect(ReviewStatus.Unreviewed) }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(
            1.dp,
            if (selected) SpecInspectColors.Ink else SpecInspectColors.Muted.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatsLine(stats: ScopeReviewStats) {
    Text(
        text = "Сценарии: ${stats.totalScenarios} • ${stats.correctScenarios} корректных • ${stats.incorrectScenarios} некорректных • ${stats.unreviewedScenarios} неоцененных",
        color = SpecInspectColors.Muted,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun statsMatch(stats: ScopeReviewStats, filter: ReviewStatus?): Boolean = when (filter) {
    null -> true
    ReviewStatus.Correct -> stats.correctScenarios > 0 && stats.incorrectScenarios == 0 && stats.unreviewedScenarios == 0
    ReviewStatus.Incorrect -> stats.incorrectScenarios > 0
    ReviewStatus.Unreviewed -> stats.unreviewedScenarios > 0
}

@Composable
private fun PlaceholderScreen(label: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecInspectColors.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = SpecInspectColors.Ink)
    }
}
