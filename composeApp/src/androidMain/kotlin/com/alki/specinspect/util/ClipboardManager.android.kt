package com.alki.specinspect.util

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context

/**
 * Android реализация менеджера буфера обмена
 */
actual object ClipboardManager {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    actual fun copyToClipboard(text: String) {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager
        val clip = ClipData.newPlainText("ShareID", text)
        clipboardManager?.setPrimaryClip(clip)
    }
}
