package com.alki.salalads.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataCreate
import platform.CoreGraphics.*
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

/**
 * iOS реализация менеджера шаринга изображений
 */
actual object ImageSharing {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun shareImage(imageBitmap: ImageBitmap, title: String) {
        withContext(Dispatchers.Default) {
            try {
                // Конвертируем ImageBitmap в Skia Bitmap
                val skiaBitmap = imageBitmap.asSkiaBitmap()

                val width = skiaBitmap.width
                val height = skiaBitmap.height
                val pixels = skiaBitmap.readPixels(
                    ImageInfo(
                        width = width,
                        height = height,
                        colorType = ColorType.RGBA_8888,
                        alphaType = ColorAlphaType.PREMUL
                    )
                ) ?: return@withContext

                // Создаем CFData из пикселей
                val cfData = pixels.usePinned { pinned ->
                    CFDataCreate(
                        null,
                        pinned.addressOf(0).reinterpret(),
                        (width * height * 4).toLong()
                    )
                }

                // Создаем CGDataProvider
                val dataProvider = CGDataProviderCreateWithCFData(cfData)

                // Создаем CGImage
                val colorSpace = CGColorSpaceCreateDeviceRGB()
                val cgImage = CGImageCreate(
                    width = width.toULong(),
                    height = height.toULong(),
                    bitsPerComponent = 8u,
                    bitsPerPixel = 32u,
                    bytesPerRow = (width * 4).toULong(),
                    space = colorSpace,
                    bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
                    provider = dataProvider,
                    decode = null,
                    shouldInterpolate = false,
                    intent = CGColorRenderingIntent.kCGRenderingIntentDefault
                )

                // Создаем UIImage из CGImage
                val uiImage = cgImage?.let { UIImage.imageWithCGImage(it) } ?: return@withContext

                // Показываем шаринг на главном потоке
                withContext(Dispatchers.Main) {
                    val activityViewController = UIActivityViewController(
                        activityItems = listOf(uiImage),
                        applicationActivities = null
                    )

                    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                    rootViewController?.presentViewController(
                        activityViewController,
                        animated = true,
                        completion = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
