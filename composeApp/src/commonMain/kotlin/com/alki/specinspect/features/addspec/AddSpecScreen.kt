package com.alki.specinspect.features.addspec

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.PrimaryButton
import com.alki.specinspect.ui.components.TintedCard
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown

@Composable
fun AddSpecScreen(component: AddSpecComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppTopBar(title = "Добавить спецификацию", onBack = component::onBack)

            WhiteCard {
                TokenField(
                    value = state.userAccessToken,
                    onValueChange = component::onUserAccessTokenChanged,
                    onGenerateClick = component::onGenerateToken,
                )
            }

            WhiteCard {
                DropdownField(
                    label = "Репозиторий",
                    placeholder = "Выберите репозиторий",
                    value = state.repositoryUrl,
                    options = state.repositories.map { it.fullName },
                    enabled = state.repositories.isNotEmpty() && !state.isRepositoriesLoading,
                    supportingText = state.repositoriesStatusMessage,
                    supportingTextIsError = state.repositoriesStatusIsError,
                    onValueChange = component::onRepositoryUrlChanged,
                )
            }

            WhiteCard {
                DropdownField(
                    label = "Ветка",
                    placeholder = "Выберите ветку",
                    value = state.branch,
                    options = state.branches,
                    enabled = state.branches.isNotEmpty() && !state.isBranchesLoading,
                    supportingText = state.branchesStatusMessage,
                    supportingTextIsError = state.branchesStatusIsError,
                    onValueChange = component::onBranchChanged,
                )
            }

            WhiteCard {
                LabeledField(
                    label = "Путь до спецификации",
                    placeholder = "openspec/specs",
                    value = state.specificationPath,
                    onValueChange = component::onSpecificationPathChanged,
                )
            }

            TintedCard(
                background = AppColors.TealSurface,
                border = AppColors.TealBorder,
                padding = PaddingValues(20.dp),
            ) {
                Text("Как работает импорт", style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Приложение обращается к GitHub REST Contents API, использует user access token, обходит указанную папку " +
                        "и загружает каждый найденный `spec.md` как отдельную подспецификацию.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.GreyViolet,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Список репозиториев подтягивается сразу после ввода токена, а список веток - после выбора репозитория.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.GreyViolet,
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.White.copy(alpha = 0.7f))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "openspec/\n└─ specs/\n   ├─ dashboard/\n   │ └─ spec.md\n   ├─ events-feed/\n   │ └─ spec.md",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = AppColors.Dark,
                    )
                }
            }

            WhiteCard {
                LabeledField(
                    label = "Название спецификации",
                    placeholder = "Имя репозитория",
                    value = state.name,
                    onValueChange = component::onNameChanged,
                )
            }

            if (state.errorMessage != null) {
                Text(
                    state.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Coral,
                )
            }

            PrimaryButton(
                text = if (state.isLoading) "Добавляем..." else "Добавить спецификацию",
                onClick = component::onSubmit,
                enabled = state.canSubmit,
            )
        }
    }
}

@Composable
private fun TokenField(
    value: String,
    onValueChange: (String) -> Unit,
    onGenerateClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("User access token", style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                InputFieldBox {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        cursorBrush = SolidColor(AppColors.Dark),
                        textStyle = LocalTextStyle.current.copy(color = AppColors.Dark),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (value.isEmpty()) {
                                Text(
                                    "ghu_...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.Dark.copy(alpha = 0.5f),
                                )
                            }
                            inner()
                        },
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            GenerateButton(onClick = onGenerateClick)
        }
    }
}

@Composable
private fun GenerateButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(112.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.White)
            .border(1.dp, AppColors.CardBorderStrong, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("Generate", style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
    }
}

@Composable
private fun DropdownField(
    label: String,
    placeholder: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    supportingText: String? = null,
    supportingTextIsError: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            InputFieldBox(
                borderColor = if (supportingTextIsError) AppColors.Coral else AppColors.CardBorderStrong,
                modifier = Modifier.clickable(enabled = enabled) { expanded = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (value.isNotEmpty()) value else placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (value.isNotEmpty()) AppColors.Dark else AppColors.Dark.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = TablerIcons.ChevronDown,
                        contentDescription = null,
                        tint = if (enabled) AppColors.Dark else AppColors.GreyViolet,
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            expanded = false
                            onValueChange(option)
                        },
                    )
                }
            }
        }
        if (supportingText != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (supportingTextIsError) AppColors.Coral else AppColors.GreyViolet,
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
        Spacer(Modifier.height(8.dp))
        InputFieldBox {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Dark),
                textStyle = LocalTextStyle.current.copy(color = AppColors.Dark),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Dark.copy(alpha = 0.5f),
                        )
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun MultilineField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    minHeight: Int,
    monoFont: Boolean = false,
) {
    val style = LocalTextStyle.current.copy(
        color = AppColors.Dark,
        fontFamily = if (monoFont) FontFamily.Monospace else null,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightAtLeast(minHeight)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AppColors.CardBorderStrong, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            cursorBrush = SolidColor(AppColors.Dark),
            textStyle = style,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = if (monoFont) FontFamily.Monospace else null
                        ),
                        color = AppColors.Dark.copy(alpha = 0.4f),
                    )
                }
                inner()
            },
        )
    }
}

private fun Modifier.heightAtLeast(min: Int): Modifier =
    this.then(Modifier.height(min.dp))

@Composable
private fun InputFieldBox(
    modifier: Modifier = Modifier,
    borderColor: androidx.compose.ui.graphics.Color = AppColors.CardBorderStrong,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}
