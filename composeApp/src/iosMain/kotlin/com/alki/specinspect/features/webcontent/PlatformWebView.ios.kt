package com.alki.specinspect.features.webcontent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration(),
            ).apply {
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.URL?.absoluteString != url) {
                webView.loadUrl(url)
            }
        },
    )
}

private fun WKWebView.loadUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    loadRequest(NSURLRequest.requestWithURL(nsUrl))
}
