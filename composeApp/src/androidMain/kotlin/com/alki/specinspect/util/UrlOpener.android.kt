package com.alki.specinspect.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Android реализация менеджера открытия URL
 */
actual object UrlOpener {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    actual fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context?.startActivity(intent)
    }
}
