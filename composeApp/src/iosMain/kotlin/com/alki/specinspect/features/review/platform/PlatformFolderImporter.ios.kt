package com.alki.specinspect.features.review.platform

import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.RawImportedSpecification
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.lastPathComponent
import platform.Foundation.stringWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual object PlatformFolderImporter {
    private var pendingContinuation: ((RawImportedLibrary?) -> Unit)? = null
    private var pickerDelegate: FolderPickerDelegate? = null

    actual val isSupported: Boolean = true

    @OptIn(BetaInteropApi::class)
    actual suspend fun pickLibrary(): RawImportedLibrary? =
        suspendCancellableCoroutine { continuation ->
            val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (rootController == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            pendingContinuation = { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            continuation.invokeOnCancellation {
                pendingContinuation = null
                pickerDelegate = null
            }

            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.folder"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen
            )
            val delegate = FolderPickerDelegate(
                onPicked = { url ->
                    complete(importFromDirectory(url))
                },
                onCancelled = {
                    complete(null)
                }
            )
            pickerDelegate = delegate
            picker.delegate = delegate

            rootController.presentViewController(
                viewControllerToPresent = picker,
                animated = true,
                completion = null
            )
        }

    private fun complete(result: RawImportedLibrary?) {
        pendingContinuation?.invoke(result)
        pendingContinuation = null
        pickerDelegate = null
    }

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    private fun importFromDirectory(root: NSURL): RawImportedLibrary? {
        val fileManager = NSFileManager.defaultManager
        val specsUrl = resolveSpecsDirectory(root, fileManager) ?: return null

        val subspecUrls = fileManager.contentsOfDirectoryAtURL(
            url = specsUrl,
            includingPropertiesForKeys = null,
            options = 0u,
            error = null
        ) ?: return null

        val specs = mutableListOf<RawImportedSpecification>()
        for (entry in subspecUrls) {
            val folderUrl = entry as? NSURL ?: continue
            val specUrl = folderUrl.URLByAppendingPathComponent("spec.md") ?: continue
            val markdown = NSString.stringWithContentsOfURL(specUrl, encoding = 4u, error = null) as String?
            if (!markdown.isNullOrBlank()) {
                val folderName = folderUrl.lastPathComponent ?: "subspec"
                specs += RawImportedSpecification(
                    folderName = folderName,
                    markdown = markdown
                )
            }
        }

        if (specs.isEmpty()) return null

        return RawImportedLibrary(
            sourceName = root.lastPathComponent ?: "Imported Specs",
            sourceType = LibrarySourceType.Custom,
            specifications = specs
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun resolveSpecsDirectory(root: NSURL, fileManager: NSFileManager): NSURL? {
        val rootName = root.lastPathComponent
        if (rootName == "specs") return root
        val nestedSpecs = root.URLByAppendingPathComponent("specs") ?: return null
        val exists = fileManager.fileExistsAtPath(nestedSpecs.path ?: return null)
        return if (exists) nestedSpecs else null
    }
}

private class FolderPickerDelegate(
    private val onPicked: (NSURL) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val picked = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (picked != null) {
            onPicked(picked)
        } else {
            onCancelled()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelled()
    }
}
