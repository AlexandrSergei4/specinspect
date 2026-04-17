package com.alki.salalads.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Кроссплатформенный менеджер для шаринга изображений
 */
expect object ImageSharing {
    /**
     * Делится изображением через системный шаринг
     * @param imageBitmap изображение для шаринга
     * @param title заголовок для шаринга
     */
    suspend fun shareImage(imageBitmap: ImageBitmap, title: String)
}
