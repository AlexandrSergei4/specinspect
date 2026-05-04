package com.alki.specinspect.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android реализация менеджера шаринга изображений
 */
actual object ImageSharing {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    actual suspend fun shareImage(imageBitmap: ImageBitmap, title: String) {
        val ctx = context ?: return

        withContext(Dispatchers.IO) {
            try {
                // Конвертируем ImageBitmap в Android Bitmap
                val bitmap = imageBitmap.asAndroidBitmap()

                // Создаем временный файл
                val cachePath = File(ctx.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "statistics_${System.currentTimeMillis()}.png")

                // Сохраняем bitmap в файл
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                // Получаем URI через FileProvider
                val contentUri = FileProvider.getUriForFile(
                    ctx,
                    "${ctx.packageName}.fileprovider",
                    file
                )

                // Создаем Intent для шаринга
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Запускаем шаринг
                ctx.startActivity(Intent.createChooser(shareIntent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    actual suspend fun shareText(text: String, title: String) {
        val ctx = context ?: return

        withContext(Dispatchers.Main) {
            try {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_TITLE, title)
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                ctx.startActivity(Intent.createChooser(shareIntent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
