package com.alki.specinspect.util

import androidx.compose.ui.graphics.ImageBitmap

actual object ImageSharing {
    actual suspend fun shareImage(imageBitmap: ImageBitmap, title: String) {
        // Desktop implementation can be upgraded to native share sheet later.
    }
}

