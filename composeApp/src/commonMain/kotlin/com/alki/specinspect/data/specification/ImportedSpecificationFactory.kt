package com.alki.specinspect.data.specification

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.openspec.OpenSpecParser
import com.alki.specinspect.data.speckit.SpecKitParser
import kotlin.random.Random

object ImportedSpecificationFactory {

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

                val idPrefix = "user-$specSeed-$index"
                val specKitSubspec = SpecKitParser.parseSubspec(
                    name = subspecName,
                    content = file.content,
                    idPrefix = idPrefix,
                )
                if (specKitSubspec.requirements.isNotEmpty()) {
                    specKitSubspec
                } else {
                    val openSpecSubspec = OpenSpecParser.parseSubspec(
                        name = subspecName,
                        content = file.content,
                        idPrefix = idPrefix,
                    )
                    if (openSpecSubspec.requirements.isEmpty()) {
                        error(
                            "Не удалось распознать use cases spec-kit или Requirement в $subspecName/spec.md",
                        )
                    }
                    openSpecSubspec
                }
            }

        return Specification(
            id = "user-spec-$specSeed",
            name = specName,
            isDemo = false,
            subspecs = subspecs,
        )
    }
}
