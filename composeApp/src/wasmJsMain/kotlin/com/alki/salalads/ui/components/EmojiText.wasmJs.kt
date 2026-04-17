@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.alki.salalads.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.await
import org.jetbrains.skia.Image
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Рендерит emoji через браузерный Canvas и возвращает data URL
 */
private fun renderEmojiToDataUrl(emoji: String, size: Int): Promise<JsAny?> = js("""(function() {
    return new Promise(function(resolve) {
        var canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        var ctx = canvas.getContext('2d');
        ctx.font = size * 0.85 + 'px "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", "EmojiOne Color", sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(emoji, size / 2, size / 2 + size * 0.05);
        resolve(canvas.toDataURL('image/png'));
    });
})()""")

/**
 * Загружает изображение по data URL и возвращает как ArrayBuffer
 */
private fun fetchImageBytes(dataUrl: JsAny): Promise<JsAny?> = js("""(function() {
    return fetch(dataUrl)
        .then(function(response) { return response.arrayBuffer(); })
        .then(function(buffer) { return new Uint8Array(buffer); });
})()""")

/**
 * Получает длину Uint8Array
 */
private fun getArrayLength(array: JsAny): Int = js("array.length")

/**
 * Получает байт из Uint8Array по индексу
 */
private fun getArrayByte(array: JsAny, index: Int): Byte = js("array[index]")

/**
 * Конвертирует JsAny Uint8Array в ByteArray
 */
private fun jsArrayToByteArray(jsArray: JsAny): ByteArray {
    val length = getArrayLength(jsArray)
    return ByteArray(length) { i -> getArrayByte(jsArray, i) }
}

/**
 * На WASM рендерим emoji через браузерный Canvas
 * Браузер использует системные emoji шрифты (Apple Color Emoji, Segoe UI Emoji и т.д.)
 */
@Composable
actual fun EmojiText(
    text: String,
    modifier: Modifier,
    fontSize: TextUnit
) {
    // Размер в пикселях для рендеринга (с запасом для качества)
    val sizeInPx = (fontSize.value * 2).toInt().coerceAtLeast(48)

    var imageBitmap by remember(text, sizeInPx) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(text, sizeInPx) {
        try {
            // Рендерим emoji в Canvas и получаем data URL
            val dataUrl = renderEmojiToDataUrl(text, sizeInPx).await<JsAny>()
            if (dataUrl != null) {
                // Загружаем изображение как байты
                val bytesJs = fetchImageBytes(dataUrl).await<JsAny>()
                if (bytesJs != null) {
                    val bytes = jsArrayToByteArray(bytesJs)
                    // Создаем Skia Image и конвертируем в ImageBitmap
                    val skiaImage = Image.makeFromEncoded(bytes)
                    imageBitmap = skiaImage.toComposeImageBitmap()
                }
            }
        } catch (e: Exception) {
            println("EmojiText error: ${e.message}")
        }
    }

    // Размер компонента в dp
    val sizeDp = fontSize.value.dp

    Canvas(modifier = modifier.size(sizeDp)) {
        imageBitmap?.let { bitmap ->
            drawImage(
                image = bitmap,
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }
}
