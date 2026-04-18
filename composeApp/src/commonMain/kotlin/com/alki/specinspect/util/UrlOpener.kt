package com.alki.specinspect.util

/**
 * Кроссплатформенный менеджер открытия URL
 */
expect object UrlOpener {
    /**
     * Открывает URL во внешнем браузере
     * @param url URL для открытия
     */
    fun openUrl(url: String)
}
