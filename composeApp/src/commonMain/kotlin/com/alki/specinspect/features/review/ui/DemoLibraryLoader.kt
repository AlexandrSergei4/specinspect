package com.alki.specinspect.features.review.ui

import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.RawImportedSpecification
import org.jetbrains.compose.resources.ExperimentalResourceApi
import specinspect.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
suspend fun loadDemoLibrary(): RawImportedLibrary {
    val bytes = Res.readBytes("files/demo-specification/spec.md")
    return RawImportedLibrary(
        sourceName = "Demo Specification",
        sourceType = LibrarySourceType.Demo,
        specifications = listOf(
            RawImportedSpecification(
                folderName = "demo-specification",
                markdown = bytes.decodeToString()
            )
        )
    )
}
