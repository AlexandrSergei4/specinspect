package com.alki.specinspect.features.review.platform

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.RawImportedSpecification
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual object PlatformFolderImporter {
    private var activity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<Uri?>? = null
    private var pendingResult: ((RawImportedLibrary?) -> Unit)? = null

    fun init(hostActivity: ComponentActivity) {
        activity = hostActivity
        if (launcher == null) {
            launcher = hostActivity.registerForActivityResult(OpenDocumentTree()) { uri ->
                pendingResult?.invoke(uri?.let { importTree(it) })
                pendingResult = null
            }
        }
    }

    actual val isSupported: Boolean
        get() = activity != null && launcher != null

    actual suspend fun pickLibrary(): RawImportedLibrary? =
        suspendCancellableCoroutine { continuation ->
            val activeLauncher = launcher
            if (activeLauncher == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            pendingResult = { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            continuation.invokeOnCancellation {
                pendingResult = null
            }

            activeLauncher.launch(null)
        }

    @SuppressLint("Range")
    private fun importTree(treeUri: Uri): RawImportedLibrary? {
        val activeActivity = activity ?: return null
        val contentResolver = activeActivity.contentResolver
        val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
        val childDocumentsUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)

        activeActivity.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val specs = mutableListOf<RawImportedSpecification>()
        contentResolver.query(
            childDocumentsUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(0)
                val displayName = cursor.getString(1)
                val mimeType = cursor.getString(2)
                if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR) continue

                val directoryUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                val directoryChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
                val specFileUri = findSpecFileUri(directoryChildrenUri)
                val markdown = specFileUri?.let { fileUri ->
                    contentResolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() }
                }

                if (!markdown.isNullOrBlank()) {
                    specs += RawImportedSpecification(
                        folderName = displayName ?: documentId.substringAfterLast('/'),
                        markdown = markdown
                    )
                }
            }
        }

        if (specs.isEmpty()) return null

        val rootName = queryDisplayName(rootDocumentUri) ?: "Imported Specs"
        return RawImportedLibrary(
            sourceName = rootName,
            sourceType = LibrarySourceType.Custom,
            specifications = specs
        )
    }

    private fun findSpecFileUri(childrenUri: Uri): Uri? {
        val activeActivity = activity ?: return null
        val contentResolver = activeActivity.contentResolver
        contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childDocumentId = cursor.getString(0)
                val displayName = cursor.getString(1)
                if (displayName == "spec.md") {
                    return DocumentsContract.buildDocumentUriUsingTree(childrenUri, childDocumentId)
                }
            }
        }
        return null
    }

    private fun queryDisplayName(documentUri: Uri): String? {
        val activeActivity = activity ?: return null
        val contentResolver = activeActivity.contentResolver
        contentResolver.query(
            documentUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }
}
