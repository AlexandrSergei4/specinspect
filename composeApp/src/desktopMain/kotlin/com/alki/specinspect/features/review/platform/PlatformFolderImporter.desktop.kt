package com.alki.specinspect.features.review.platform

import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.RawImportedSpecification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

actual object PlatformFolderImporter {
    actual val isSupported: Boolean = true

    actual suspend fun pickLibrary(): RawImportedLibrary? = withContext(Dispatchers.IO) {
        runCatching {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }

        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select specs folder or project folder with specs"
        }

        val result = chooser.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) return@withContext null

        importFromDirectory(chooser.selectedFile)
    }

    private fun importFromDirectory(selectedDirectory: File): RawImportedLibrary? {
        if (!selectedDirectory.exists() || !selectedDirectory.isDirectory) return null

        val specsDir = when {
            selectedDirectory.name == "specs" -> selectedDirectory
            File(selectedDirectory, "specs").isDirectory -> File(selectedDirectory, "specs")
            else -> return null
        }

        val specifications = specsDir
            .listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isDirectory }
            .mapNotNull { subspecDir ->
                val specFile = File(subspecDir, "spec.md")
                if (!specFile.exists() || !specFile.isFile) return@mapNotNull null

                val markdown = runCatching { specFile.readText() }.getOrNull()
                if (markdown.isNullOrBlank()) return@mapNotNull null

                RawImportedSpecification(
                    folderName = subspecDir.name,
                    markdown = markdown
                )
            }
            .toList()

        if (specifications.isEmpty()) return null

        return RawImportedLibrary(
            sourceName = selectedDirectory.name,
            sourceType = LibrarySourceType.Custom,
            specifications = specifications
        )
    }
}

