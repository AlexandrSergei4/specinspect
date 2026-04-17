package com.alki.salalads.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS реализация менеджера открытия URL
 */
actual object UrlOpener {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
