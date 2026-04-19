package com.alki.specinspect.features.review

import kotlin.time.Clock

class OpenSpecParser {

    fun parseLibrary(rawLibrary: RawImportedLibrary): ImportedSpecLibrary {
        val specId = stableId(rawLibrary.sourceName)
        val subspecs = rawLibrary.specifications.mapNotNull { rawSubspec ->
            parseSubspec(specId = specId, specificationName = rawLibrary.sourceName, rawSubspec = rawSubspec)
        }

        val specifications = if (subspecs.isEmpty()) {
            emptyList()
        } else {
            listOf(
                ImportedSpecification(
                    specId = specId,
                    folderName = rawLibrary.sourceName.toFolderName(),
                    displayName = rawLibrary.sourceName,
                    subspecs = subspecs
                )
            )
        }

        return ImportedSpecLibrary(
            sourceName = rawLibrary.sourceName,
            sourceType = rawLibrary.sourceType,
            importedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            specifications = specifications
        )
    }

    private fun parseSubspec(
        specId: String,
        specificationName: String,
        rawSubspec: RawImportedSpecification
    ): ImportedSubspec? {
        val requirements = parseRequirements(
            specId = specId,
            specificationName = specificationName,
            subspecFolderName = rawSubspec.folderName,
            markdown = rawSubspec.markdown
        )
        if (requirements.isEmpty()) return null

        return ImportedSubspec(
            subspecId = stableId("$specId|${rawSubspec.folderName}"),
            specId = specId,
            folderName = rawSubspec.folderName,
            displayName = rawSubspec.folderName.toDisplayName(),
            requirements = requirements
        )
    }

    private fun parseRequirements(
        specId: String,
        specificationName: String,
        subspecFolderName: String,
        markdown: String
    ): List<ImportedRequirement> {
        val lines = markdown.lines()
        val requirements = mutableListOf<ImportedRequirement>()
        var index = 0
        val subspecId = stableId("$specId|$subspecFolderName")
        val subspecName = subspecFolderName.toDisplayName()

        while (index < lines.size) {
            val line = lines[index].trim()
            if (!line.startsWith("### Requirement:")) {
                index++
                continue
            }

            val requirementTitle = line.removePrefix("### Requirement:").trim()
            index++

            val descriptionLines = mutableListOf<String>()
            val scenarios = mutableListOf<ImportedScenario>()

            while (index < lines.size) {
                val current = lines[index].trim()
                when {
                    current.startsWith("### Requirement:") -> break
                    current.startsWith("#### Scenario:") -> {
                        val scenarioTitle = current.removePrefix("#### Scenario:").trim()
                        index++
                        var whenText = ""
                        var thenText = ""

                        while (index < lines.size) {
                            val scenarioLine = lines[index].trim()
                            if (scenarioLine.startsWith("#### Scenario:") || scenarioLine.startsWith("### Requirement:")) {
                                break
                            }
                            when {
                                scenarioLine.startsWith("- **WHEN**") -> {
                                    whenText = scenarioLine.removePrefix("- **WHEN**").trim()
                                }
                                scenarioLine.startsWith("- **THEN**") -> {
                                    thenText = scenarioLine.removePrefix("- **THEN**").trim()
                                }
                            }
                            index++
                        }

                        if (whenText.isNotBlank() || thenText.isNotBlank()) {
                            val requirementId = stableId("$specId|$subspecFolderName|$requirementTitle")
                            scenarios += ImportedScenario(
                                scenarioId = stableId(
                                    "$requirementId|$scenarioTitle|$whenText|$thenText"
                                ),
                                requirementId = requirementId,
                                title = scenarioTitle,
                                whenText = whenText,
                                thenText = thenText
                            )
                        }
                        continue
                    }
                    current.isNotBlank() -> descriptionLines += current
                }
                index++
            }

            if (requirementTitle.isBlank() || scenarios.isEmpty()) {
                continue
            }

            val description = descriptionLines.joinToString("\n").ifBlank {
                "No additional requirement description provided."
            }
            val requirementId = stableId("$specId|$subspecFolderName|$requirementTitle|$description")
            val cardId = stableId(
                buildString {
                    append(subspecFolderName)
                    append('|')
                    append(requirementTitle)
                    append('|')
                    append(description)
                    append('|')
                    scenarios.forEach { scenario ->
                        append(scenario.title)
                        append('|')
                        append(scenario.whenText)
                        append('|')
                        append(scenario.thenText)
                        append('|')
                    }
                }
            )
            requirements += ImportedRequirement(
                requirementId = requirementId,
                cardId = cardId,
                specId = specId,
                subspecId = subspecId,
                specificationName = specificationName,
                subspecName = subspecName,
                requirementTitle = requirementTitle,
                requirementDescription = description,
                scenarios = scenarios.map { it.copy(requirementId = requirementId) }
            )
        }

        return requirements
    }
}

internal fun stableId(value: String): String {
    val normalized = value.trim().lowercase()
    return normalized.hashCode().toUInt().toString(16)
}

private fun String.toDisplayName(): String =
    split('-', '_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { char -> char.uppercase() }
        }

private fun String.toFolderName(): String =
    trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
