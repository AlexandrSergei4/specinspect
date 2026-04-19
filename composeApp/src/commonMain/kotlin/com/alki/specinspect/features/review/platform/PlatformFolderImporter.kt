package com.alki.specinspect.features.review.platform

import com.alki.specinspect.features.review.RawImportedLibrary

expect object PlatformFolderImporter {
    val isSupported: Boolean
    suspend fun pickLibrary(): RawImportedLibrary?
}
