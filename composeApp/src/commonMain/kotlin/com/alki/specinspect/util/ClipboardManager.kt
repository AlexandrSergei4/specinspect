package com.alki.specinspect.util

/**
 * Кроссплатформенный менеджер буфера обмена
 */
expect object ClipboardManager {
    /**
     * Копирует текст в буфер обмена
     * @param text текст для копирования
     */
    fun copyToClipboard(text: String)
}
