package com.alki.specinspect.util

import java.awt.Desktop
import java.net.URI

actual object UrlOpener {
    actual fun openUrl(url: String) {
        if (!Desktop.isDesktopSupported()) return
        runCatching {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

