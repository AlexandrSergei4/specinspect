package com.alki.specinspect.features.review.platform

import com.alki.specinspect.features.review.RawImportedLibrary

actual object PlatformFolderImporter {
    actual val isSupported: Boolean = false

    actual suspend fun pickLibrary(): RawImportedLibrary? = null
}
