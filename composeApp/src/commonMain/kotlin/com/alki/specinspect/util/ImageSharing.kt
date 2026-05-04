package com.alki.specinspect.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Кроссплатформенный менеджер для системного шаринга.
 */
expect object ImageSharing {
    /**
     * Делится изображением через системный шаринг
     * @param imageBitmap изображение для шаринга
     * @param title заголовок для шаринга
     */
    suspend fun shareImage(imageBitmap: ImageBitmap, title: String)

    /**
     * Делится текстом через системный шаринг
     * @param text текст для шаринга
     * @param title заголовок для шаринга
     */
    suspend fun shareText(text: String, title: String)
}
