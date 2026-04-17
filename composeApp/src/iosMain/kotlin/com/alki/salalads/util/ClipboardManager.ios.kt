package com.alki.salalads.util

import platform.UIKit.UIPasteboard

/**
 * iOS реализация менеджера буфера обмена
 */
actual object ClipboardManager {
    actual fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
