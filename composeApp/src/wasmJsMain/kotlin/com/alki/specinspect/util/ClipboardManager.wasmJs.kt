package com.alki.specinspect.util

import kotlinx.browser.window

/**
 * Реализация ClipboardManager для WASM (браузер)
 * Использует Clipboard API браузера
 */
actual object ClipboardManager {
    /**
     * Копирует текст в буфер обмена через Clipboard API
     */
    actual fun copyToClipboard(text: String) {
        window.navigator.clipboard.writeText(text)
    }
}
