package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.models.Specification
import kotlin.random.Random

object OpenSpecSpecificationFactory {

    fun create(name: String, files: List<ImportedSpecFile>): Specification {
        val specName = name.trim()
        if (specName.isEmpty()) error("Укажите имя спецификации")
        if (files.isEmpty()) error("По указанному пути не найдено ни одного spec.md")

        val specSeed = Random.nextInt(100000, 999999)
        val subspecs = files
            .sortedBy { it.name.lowercase() }
            .mapIndexed { index, file ->
                val subspecName = file.name.trim()
                if (subspecName.isEmpty()) error("Не удалось определить имя подспецификации")
                val subspec = OpenSpecParser.parseSubspec(
                    name = subspecName,
                    content = file.content,
                    idPrefix = "user-$specSeed-$index",
                )
                if (subspec.requirements.isEmpty()) {
                    error("Не удалось распознать Requirement в ${subspecName}/spec.md")
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
