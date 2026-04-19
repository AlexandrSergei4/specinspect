package com.alki.specinspect.features.review

import kotlin.time.Clock

class OpenSpecParser {

    fun parseLibrary(rawLibrary: RawImportedLibrary): ImportedSpecLibrary {
        val specifications = rawLibrary.specifications.mapNotNull { rawSpec ->
            parseSpecification(rawSpec)
        }

        return ImportedSpecLibrary(
            sourceName = rawLibrary.sourceName,
            sourceType = rawLibrary.sourceType,
            importedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            specifications = specifications
        )
    }

    private fun parseSpecification(rawSpec: RawImportedSpecification): ImportedSpecification? {
        val requirements = parseRequirements(rawSpec.folderName, rawSpec.markdown)
        if (requirements.isEmpty()) return null

        val specId = stableId(rawSpec.folderName)
        return ImportedSpecification(
            specId = specId,
            folderName = rawSpec.folderName,
            displayName = rawSpec.folderName.toDisplayName(),
            requirements = requirements.map { it.copy(specId = specId) }
        )
    }

    private fun parseRequirements(folderName: String, markdown: String): List<RequirementCard> {
        val lines = markdown.lines()
        val requirements = mutableListOf<RequirementCard>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index].trim()
            if (!line.startsWith("### Requirement:")) {
                index++
                continue
            }

            val requirementTitle = line.removePrefix("### Requirement:").trim()
            index++

            val descriptionLines = mutableListOf<String>()
            val scenarios = mutableListOf<RequirementScenario>()

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
                            scenarios += RequirementScenario(
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

            if (requirementTitle.isNotBlank() && scenarios.isNotEmpty()) {
                val description = descriptionLines.joinToString("\n").ifBlank {
                    "No additional requirement description provided."
                }
                val fingerprint = buildString {
                    append(folderName)
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

                requirements += RequirementCard(
                    cardId = stableId(fingerprint),
                    specId = "",
                    specificationName = folderName.toDisplayName(),
                    requirementTitle = requirementTitle,
                    requirementDescription = description,
                    scenarios = scenarios
                )
            }
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
