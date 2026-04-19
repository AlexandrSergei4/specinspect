package com.alki.specinspect.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object ClipboardManager {
    actual fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }
}

