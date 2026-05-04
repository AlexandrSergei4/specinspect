package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.localizedError
import kotlin.random.Random

object OpenSpecSpecificationFactory {

    fun create(name: String, files: List<ImportedSpecFile>): Specification {
        val specName = name.trim()
        if (specName.isEmpty()) localizedError(AppTextKey.ErrorSpecNameRequired)
        if (files.isEmpty()) localizedError(AppTextKey.ErrorNoSpecFiles)

        val specSeed = Random.nextInt(100000, 999999)
        val subspecs = files
            .sortedBy { it.name.lowercase() }
            .mapIndexed { index, file ->
                val subspecName = file.name.trim()
                if (subspecName.isEmpty()) localizedError(AppTextKey.ErrorSubspecNameMissing)
                val subspec = OpenSpecParser.parseSubspec(
                    name = subspecName,
                    content = file.content,
                    idPrefix = "user-$specSeed-$index",
                )
                if (subspec.requirements.isEmpty()) {
                    localizedError(AppTextKey.ErrorOpenSpecRequirementParseFailed, subspecName)
                }
                subspec
            }

        return Specification(
            id = "user-spec-$specSeed",
            name = specName,
            isDemo = false,
            subspecs = subspecs,
        )
    }
}
