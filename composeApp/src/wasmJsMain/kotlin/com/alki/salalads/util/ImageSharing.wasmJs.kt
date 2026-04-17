@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.alki.salalads.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Внешние интерфейсы для работы с Canvas и Web Share API
 */
external interface JsBlob : JsAny
external interface JsFile : JsAny
external interface JsCanvas : JsAny
external interface JsCanvasContext : JsAny
external interface JsImageData : JsAny
external interface JsUint8ClampedArray : JsAny

// Canvas операции
private fun createCanvas(width: Int, height: Int): JsCanvas =
    js("(function() { var c = document.createElement('canvas'); c.width = width; c.height = height; return c; })()")

private fun getContext2d(canvas: JsCanvas): JsCanvasContext =
    js("canvas.getContext('2d')")

private fun createImageData(ctx: JsCanvasContext, width: Int, height: Int): JsImageData =
    js("ctx.createImageData(width, height)")

private fun getImageDataArray(imageData: JsImageData): JsUint8ClampedArray =
    js("imageData.data")

private fun setArrayValue(array: JsUint8ClampedArray, index: Int, value: Int): Unit =
    js("array[index] = value")

private fun putImageData(ctx: JsCanvasContext, imageData: JsImageData, x: Int, y: Int): Unit =
    js("ctx.putImageData(imageData, x, y)")

private fun canvasToBlob(canvas: JsCanvas): Promise<JsBlob?> =
    js("new Promise(function(resolve) { canvas.toBlob(resolve, 'image/png'); })")

// File и Blob операции
private fun createFileFromBlob(blob: JsBlob, filename: String): JsFile =
    js("new File([blob], filename, { type: 'image/png' })")

// Web Share API
private fun canShareFiles(): Boolean =
    js("typeof navigator.share === 'function' && typeof navigator.canShare === 'function'")

private fun canShareWithFile(file: JsFile): Boolean =
    js("(function() { try { return navigator.canShare({ files: [file] }); } catch(e) { return false; } })()")

private fun shareFile(file: JsFile, title: String): Promise<JsAny?> =
    js("navigator.share({ files: [file], title: title, text: title })")

// Утилиты
private fun currentTimeMillis(): Long = js("Date.now()")

// Fallback: скачивание файла
private fun downloadBlob(blob: JsBlob, filename: String): Unit = js("""
(function() {
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
})()
""")

/**
 * Реализация ImageSharing для WASM (браузер)
 * Использует Web Share API с fallback на скачивание файла
 */
actual object ImageSharing {
    /**
     * Делится изображением через Web Share API или скачивает файл
     * @param imageBitmap изображение для шаринга
     * @param title заголовок для шаринга
     */
    actual suspend fun shareImage(imageBitmap: ImageBitmap, title: String) {
        try {
            // Конвертируем ImageBitmap в Canvas
            val width = imageBitmap.width
            val height = imageBitmap.height

            val canvas = createCanvas(width, height)
            val ctx = getContext2d(canvas)
            val imageData = createImageData(ctx, width, height)
            val dataArray = getImageDataArray(imageData)

            // Копируем пиксели из ImageBitmap в ImageData
            val pixelMap = imageBitmap.toPixelMap()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = pixelMap[x, y]
                    val index = (y * width + x) * 4
                    setArrayValue(dataArray, index, (pixel.red * 255).toInt())
                    setArrayValue(dataArray, index + 1, (pixel.green * 255).toInt())
                    setArrayValue(dataArray, index + 2, (pixel.blue * 255).toInt())
                    setArrayValue(dataArray, index + 3, (pixel.alpha * 255).toInt())
                }
            }

            // Рисуем ImageData на Canvas
            putImageData(ctx, imageData, 0, 0)

            // Конвертируем Canvas в Blob
            val blob = canvasToBlob(canvas).await<JsBlob?>()
            if (blob == null) {
                println("Failed to create blob from canvas")
                return
            }

            val filename = "salalads_${currentTimeMillis()}.png"

            // Пробуем Web Share API
            if (canShareFiles()) {
                val file = createFileFromBlob(blob, filename)
                if (canShareWithFile(file)) {
                    try {
                        shareFile(file, title).await<JsAny?>()
                        return
                    } catch (e: Exception) {
                        // Web Share отменён пользователем или ошибка - переходим к скачиванию
                        println("Web Share cancelled or failed: ${e.message}")
                    }
                }
            }

            // Fallback: скачиваем файл
            downloadBlob(blob, filename)

        } catch (e: Exception) {
            println("Image sharing failed: ${e.message}")
            e.printStackTrace()
        }
    }
}
