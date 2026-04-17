package com.alki.salalads.util

import kotlinx.browser.window
import org.w3c.dom.Storage

/**
 * Вспомогательные функции для работы с localStorage в WASM
 */
object LocalStorage {
    private val storage: Storage
        get() = window.localStorage

    fun getItem(key: String): String? {
        return storage.getItem(key)
    }

    fun setItem(key: String, value: String) {
        storage.setItem(key, value)
    }

    fun removeItem(key: String) {
        storage.removeItem(key)
    }
}
