package com.alki.specinspect.data.speckit

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioSource
import com.alki.specinspect.data.models.Subspec

object SpecKitParser {

    fun parseSubspec(
        name: String,
        content: String,
        idPrefix: String = name,
        filePath: String? = null,
    ): Subspec {
        val lines = content.lines()
        val featureTitle = lines.firstNotNullOfOrNull { line ->
            FEATURE_TITLE_HEADER.matchEntire(line.trim())?.groupValues?.get(1)?.trim()
        }.orEmpty()

        val edgeCases = extractBulletedSection(lines, EDGE_CASES_HEADER)
        val functionalRequirements = extractBulletedSection(lines, FUNCTIONAL_REQUIREMENTS_HEADER)
        val stories = parseStories(lines)

        val requirements = stories.mapIndexedNotNull { index, story ->
            val scenarios = story.acceptanceScenarios.mapIndexed { scenarioIndex, scenario ->
                val parsedScenario = parseAcceptanceScenario(scenario.text)
                Scenario(
                    id = "$idPrefix::${story.title.slug()}::scenario-${scenarioIndex + 1}",
                    title = "Use Case ${scenarioIndex + 1}",
                    whenText = parsedScenario.whenText,
                    thenText = parsedScenario.thenText,
                    source = filePath
                        ?.takeIf { it.isNotBlank() }
                        ?.let { ScenarioSource(filePath = it, line = scenario.line) },
                )
            }

            if (scenarios.isEmpty()) {
                null
            } else {
                Requirement(
                    id = "$idPrefix::${story.title.slug()}::${index + 1}",
                    title = story.title,
                    description = buildRequirementDescription(
                        story = story,
                        functionalRequirements = functionalRequirements,
                        edgeCases = edgeCases,
                    ),
                    scenarios = scenarios,
                )
            }
        }

        return Subspec(
            id = idPrefix,
            name = featureTitle.ifBlank { name },
            requirements = requirements,
        )
    }

    private fun parseStories(lines: List<String>): List<ParsedStory> {
        val result = mutableListOf<ParsedStory>()
        var index = 0

        while (index < lines.size) {
            val headerMatch = USER_STORY_HEADER.matchEntire(lines[index].trim())
            if (headerMatch == null) {
                index++
                continue
            }

            val title = headerMatch.groupValues[1].trim()
            val priority = headerMatch.groupValues.getOrNull(2)?.trim().orEmpty()
            index++

            val bodyLines = mutableListOf<String>()
            var whyPriority = ""
            var independentTest = ""
            val acceptanceScenarios = mutableListOf<ParsedAcceptanceScenario>()

            while (index < lines.size) {
                val trimmed = lines[index].trim()
                if (
                    USER_STORY_HEADER.matches(trimmed) ||
                    trimmed == "---" ||
                    EDGE_CASES_HEADER.matches(trimmed) ||
                    REQUIREMENTS_HEADER.matches(trimmed)
                ) {
                    break
                }

                when {
                    trimmed.isBlank() -> {
                        bodyLines += ""
                        index++
                    }
                    WHY_PRIORITY_LINE.matches(trimmed) -> {
                        whyPriority = WHY_PRIORITY_LINE.matchEntire(trimmed)?.groupValues?.get(1)?.trim().orEmpty()
                        index++
                    }
                    INDEPENDENT_TEST_LINE.matches(trimmed) -> {
                        independentTest = INDEPENDENT_TEST_LINE.matchEntire(trimmed)?.groupValues?.get(1)?.trim().orEmpty()
                        index++
                    }
                    ACCEPTANCE_SCENARIOS_LINE.matches(trimmed) -> {
                        val collected = collectAcceptanceScenarios(lines, index + 1)
                        acceptanceScenarios += collected.items
                        index = collected.nextIndex
                    }
                    else -> {
                        bodyLines += lines[index].trimEnd()
                        index++
                    }
                }
            }

            result += ParsedStory(
                title = title,
                priority = priority,
                summary = bodyLines.joinToString("\n").trim(),
                whyPriority = whyPriority,
                independentTest = independentTest,
                acceptanceScenarios = acceptanceScenarios,
            )

            while (index < lines.size && lines[index].trim() == "---") {
                index++
            }
        }

        return result
    }

    private fun collectAcceptanceScenarios(lines: List<String>, startIndex: Int): CollectedItems {
        val items = mutableListOf<ParsedAcceptanceScenario>()
        val current = StringBuilder()
        var currentLine = -1
        var index = startIndex

        fun flushCurrent() {
            val value = current.toString().trim()
            if (value.isNotBlank()) {
                items += ParsedAcceptanceScenario(
                    text = value,
                    line = currentLine.coerceAtLeast(1),
                )
            }
            current.clear()
            currentLine = -1
        }

        while (index < lines.size) {
            val trimmed = lines[index].trim()
            if (
                USER_STORY_HEADER.matches(trimmed) ||
                trimmed == "---" ||
                EDGE_CASES_HEADER.matches(trimmed) ||
                REQUIREMENTS_HEADER.matches(trimmed) ||
                WHY_PRIORITY_LINE.matches(trimmed) ||
                INDEPENDENT_TEST_LINE.matches(trimmed) ||
                ACCEPTANCE_SCENARIOS_LINE.matches(trimmed)
            ) {
                break
            }

            val itemMatch = LIST_ITEM_LINE.matchEntire(trimmed)
            when {
                itemMatch != null -> {
                    flushCurrent()
                    currentLine = index + 1
                    current.append(itemMatch.groupValues[1].trim())
                }
                trimmed.isBlank() -> {
                    if (current.isNotEmpty()) current.append('\n')
                }
                current.isNotEmpty() -> {
                    if (!current.endsWith('\n')) current.append(' ')
                    current.append(trimmed)
                }
            }
            index++
        }

        flushCurrent()
        return CollectedItems(items = items, nextIndex = index)
    }

    private fun parseAcceptanceScenario(text: String): ParsedScenario {
        val normalized = text.replace(Regex("\\s+"), " ").trim()
        val given = GIVEN_SEGMENT.find(normalized)?.groupValues?.get(1)?.trim().orEmpty()
        val whenValue = WHEN_SEGMENT.find(normalized)?.groupValues?.get(1)?.trim().orEmpty()
        val thenValue = THEN_SEGMENT.find(normalized)?.groupValues?.get(1)?.trim().orEmpty()

        val whenText = buildString {
            if (given.isNotBlank()) append("Given ").append(given)
            if (whenValue.isNotBlank()) {
                if (isNotEmpty()) append('\n')
                append("When ").append(whenValue)
            }
        }.ifBlank { normalized }

        return ParsedScenario(
            whenText = whenText,
            thenText = thenValue.ifBlank { normalized },
        )
    }

    private fun extractBulletedSection(lines: List<String>, header: Regex): List<String> {
        val startIndex = lines.indexOfFirst { header.matches(it.trim()) }
        if (startIndex == -1) return emptyList()

        val items = mutableListOf<String>()
        val current = StringBuilder()
        var index = startIndex + 1

        fun flushCurrent() {
            val value = current.toString().trim()
            if (value.isNotBlank()) items += value
            current.clear()
        }

        while (index < lines.size) {
            val trimmed = lines[index].trim()
            if (trimmed.startsWith("## ") || (trimmed.startsWith("### ") && !header.matches(trimmed))) {
                break
            }

            val bulletMatch = BULLET_LINE.matchEntire(trimmed)
            when {
                bulletMatch != null -> {
                    flushCurrent()
                    current.append(bulletMatch.groupValues[1].trim())
                }
                trimmed.isBlank() -> {
                    flushCurrent()
                }
                current.isNotEmpty() -> {
                    current.append(' ').append(trimmed)
                }
            }
            index++
        }

        flushCurrent()
        return items
    }

    private fun buildRequirementDescription(
        story: ParsedStory,
        functionalRequirements: List<String>,
        edgeCases: List<String>,
    ): String {
        val sections = mutableListOf<String>()
        if (story.summary.isNotBlank()) sections += story.summary
        if (story.priority.isNotBlank()) sections += "Priority: ${story.priority}"
        if (story.whyPriority.isNotBlank()) sections += "Why this priority: ${story.whyPriority}"
        if (story.independentTest.isNotBlank()) sections += "Independent Test: ${story.independentTest}"
        if (functionalRequirements.isNotEmpty()) {
            sections += "Functional Requirements:\n- ${functionalRequirements.joinToString("\n- ")}"
        }
        if (edgeCases.isNotEmpty()) {
            sections += "Edge Cases:\n- ${edgeCases.joinToString("\n- ")}"
        }
        return sections.joinToString("\n\n")
    }

    private fun String.slug(): String =
        lowercase().replace(Regex("[^a-z0-9а-я]+"), "-").trim('-')

    private data class ParsedStory(
        val title: String,
        val priority: String,
        val summary: String,
        val whyPriority: String,
        val independentTest: String,
        val acceptanceScenarios: List<ParsedAcceptanceScenario>,
    )

    private data class ParsedScenario(
        val whenText: String,
        val thenText: String,
    )

    private data class ParsedAcceptanceScenario(
        val text: String,
        val line: Int,
    )

    private data class CollectedItems(
        val items: List<ParsedAcceptanceScenario>,
        val nextIndex: Int,
    )

    private val FEATURE_TITLE_HEADER = Regex("^#\\s+Feature Specification:\\s*(.+)$")
    private val USER_STORY_HEADER = Regex("^###\\s+User Story\\s+\\d+\\s*-\\s*(.+?)(?:\\s+\\(Priority:\\s*([^)]*)\\))?$")
    private val WHY_PRIORITY_LINE = Regex("^\\*\\*Why this priority\\*\\*:\\s*(.+)$")
    private val INDEPENDENT_TEST_LINE = Regex("^\\*\\*Independent Test\\*\\*:\\s*(.+)$")
    private val ACCEPTANCE_SCENARIOS_LINE = Regex("^\\*\\*Acceptance Scenarios\\*\\*:\\s*$")
    private val EDGE_CASES_HEADER = Regex("^###\\s+Edge Cases\\s*$")
    private val REQUIREMENTS_HEADER = Regex("^##\\s+Requirements\\b.*$")
    private val FUNCTIONAL_REQUIREMENTS_HEADER = Regex("^###\\s+Functional Requirements\\s*$")
    private val LIST_ITEM_LINE = Regex("^(?:[-*]|\\d+\\.)\\s+(.+)$")
    private val BULLET_LINE = Regex("^[-*]\\s+(.+)$")
    private val GIVEN_SEGMENT = Regex("\\*\\*Given\\*\\*\\s*(.+?)(?=,\\s*\\*\\*When\\*\\*|\\s+\\*\\*When\\*\\*|$)")
    private val WHEN_SEGMENT = Regex("\\*\\*When\\*\\*\\s*(.+?)(?=,\\s*\\*\\*Then\\*\\*|\\s+\\*\\*Then\\*\\*|$)")
    private val THEN_SEGMENT = Regex("\\*\\*Then\\*\\*\\s*(.+)$")
}
