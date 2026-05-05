package com.alki.specinspect.features.webcontent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.theme.AppColors

@Composable
fun WebContentScreen(component: WebContentComponent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        AppTopBar(
            title = component.title,
            onBack = component::onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.White),
        ) {
            PlatformWebView(
                url = component.url,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
)
