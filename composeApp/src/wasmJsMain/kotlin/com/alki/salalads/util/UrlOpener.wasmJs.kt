package com.alki.salalads.util

import kotlinx.browser.window

/**
 * Реализация UrlOpener для WASM (браузер)
 * Открывает URL в новой вкладке браузера
 */
actual object UrlOpener {
    /**
     * Открывает URL в новой вкладке
     */
    actual fun openUrl(url: String) {
        window.open(url, "_blank")
    }
}
