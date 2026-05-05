package com.alki.specinspect.features.myspecs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.DashedButton
import com.alki.specinspect.ui.components.IconChip
import com.alki.specinspect.ui.components.SectionHeader
import com.alki.specinspect.ui.components.SettingsIconButton
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.components.YellowPillBadge
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import compose.icons.tablericons.Upload
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_delete
import specinspect.composeapp.generated.resources.action_open
import specinspect.composeapp.generated.resources.action_settings
import specinspect.composeapp.generated.resources.badge_demo
import specinspect.composeapp.generated.resources.count_requirements
import specinspect.composeapp.generated.resources.count_specifications
import specinspect.composeapp.generated.resources.demo_specification_name
import specinspect.composeapp.generated.resources.my_specs_add
import specinspect.composeapp.generated.resources.my_specs_empty
import specinspect.composeapp.generated.resources.my_specs_loaded
import specinspect.composeapp.generated.resources.my_specs_title

@Composable
fun MySpecsScreen(component: MySpecsComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                AppTopBar(
                    title = stringResource(Res.string.my_specs_title),
                    onBack = component::onBack,
                    trailing = {
                        SettingsIconButton(
                            onClick = component::onOpenSettings,
                            contentDescription = stringResource(Res.string.action_settings),
                        )
                    },
                )
            }
            item {
                DashedButton(
                    text = stringResource(Res.string.my_specs_add),
                    onClick = component::onAdd,
                    leadingIcon = TablerIcons.Upload,
                )
            }
            item {
                SectionHeader(stringResource(Res.string.my_specs_loaded))
            }
            if (state.items.isEmpty()) {
                item {
                    WhiteCard {
                        Text(
                            stringResource(Res.string.my_specs_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.GreyViolet,
                        )
                    }
                }
            }
            items(state.items, key = { it.id }) { item ->
                SpecCard(
                    item = item,
                    displayName = if (item.isDemo) stringResource(Res.string.demo_specification_name) else item.name,
                    onOpen = { component.onOpen(item.id) },
                    onDelete = { component.onDelete(item.id) },
                )
            }
        }
    }
}

@Composable
private fun SpecCard(
    item: SpecListItem,
    displayName: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    WhiteCard(onClick = onOpen, padding = PaddingValues(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.Dark,
                    )
                    if (item.isDemo) {
                        Spacer(Modifier.width(8.dp))
                        YellowPillBadge(stringResource(Res.string.badge_demo))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        stringResource(Res.string.count_specifications, item.subspecCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.GreyViolet,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        stringResource(Res.string.count_requirements, item.requirementCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.GreyViolet,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconChip(
                icon = TablerIcons.PlayerPlay,
                onClick = onOpen,
                background = AppColors.PrimaryAction,
                iconTint = AppColors.OnPrimaryAction,
                contentDescription = stringResource(Res.string.action_open),
            )
            Spacer(Modifier.width(8.dp))
            IconChip(
                icon = TablerIcons.Trash,
                onClick = onDelete,
                background = AppColors.GreyVioletSurface,
                iconTint = AppColors.GreyViolet,
                contentDescription = stringResource(Res.string.action_delete),
            )
        }
    }
}
