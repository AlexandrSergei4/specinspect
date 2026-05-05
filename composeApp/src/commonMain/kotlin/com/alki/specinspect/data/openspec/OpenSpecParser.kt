package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioSource
import com.alki.specinspect.data.models.ScenarioStep
import com.alki.specinspect.data.models.ScenarioStepKeyword
import com.alki.specinspect.data.models.Subspec

/**
 * Парсер OpenSpec spec.md файлов.
 *
 * Ожидаемый формат:
 *
 * ## ADDED Requirements
 *
 * ### Requirement: <название>
 * <описание (одна или несколько строк)>
 *
 * #### Scenario: <название сценария>
 * - **GIVEN** <текст>
 * - **WHEN** <текст>
 *   <продолжение на следующей строке>
 * - **THEN** <текст>
 * - **AND** <текст>
 */
object OpenSpecParser {

    /**
     * Парсит содержимое одного spec.md в Subspec
     */
    fun parseSubspec(
        name: String,
        content: String,
        idPrefix: String = name,
        filePath: String? = null,
    ): Subspec {
        val lines = content.lines()
        val requirements = mutableListOf<Requirement>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val reqHeader = REQUIREMENT_HEADER.matchEntire(line.trim())
            if (reqHeader != null) {
                val title = reqHeader.groupValues[1].trim()
                val description = StringBuilder()
                val scenarios = mutableListOf<Scenario>()
                i++
                // собираем описание (до первого Scenario или Requirement)
                while (i < lines.size) {
                    val l = lines[i]
                    if (REQUIREMENT_HEADER.matches(l.trim())) break
                    if (SCENARIO_HEADER.matches(l.trim())) break
                    description.append(l).append('\n')
                    i++
                }
                // собираем все Scenario до следующего Requirement
                while (i < lines.size) {
                    val l = lines[i]
                    if (REQUIREMENT_HEADER.matches(l.trim())) break
                    val scHead = SCENARIO_HEADER.matchEntire(l.trim())
                    if (scHead != null) {
                        val scTitle = scHead.groupValues[1].trim()
                        val scenarioLine = i + 1
                        val parsedScenario = parseScenarioSteps(lines, i + 1)
                        i = parsedScenario.nextIndex
                        val whenText = parsedScenario.steps
                            .firstOrNull { it.keyword == ScenarioStepKeyword.WHEN }
                            ?.text
                            .orEmpty()
                        val thenText = parsedScenario.steps
                            .firstOrNull { it.keyword == ScenarioStepKeyword.THEN }
                            ?.text
                            .orEmpty()
                        val scId = "$idPrefix::${title.slug()}::${scTitle.slug()}::${scenarios.size}"
                        scenarios.add(
                            Scenario(
                                id = scId,
                                title = scTitle,
                                whenText = whenText,
                                thenText = thenText,
                                source = filePath
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { ScenarioSource(filePath = it, line = scenarioLine) },
                                steps = parsedScenario.steps,
                            )
                        )
                    } else {
                        i++
                    }
                }
                val reqId = "$idPrefix::${title.slug()}::${requirements.size}"
                requirements.add(
                    Requirement(
                        id = reqId,
                        title = title,
                        description = description.toString().trim(),
                        scenarios = scenarios,
                    )
                )
            } else {
                i++
            }
        }

        return Subspec(
            id = idPrefix,
            name = name,
            requirements = requirements,
        )
    }

    private fun parseScenarioSteps(lines: List<String>, startIndex: Int): ParsedScenarioSteps {
        val stepBuilders = mutableListOf<ScenarioStepBuilder>()
        var currentStep: ScenarioStepBuilder? = null
        var i = startIndex

        while (i < lines.size) {
            val trimmed = lines[i].trim()
            if (isScenarioBoundary(trimmed)) break

            val stepMatch = SCENARIO_STEP_LINE.matchEntire(trimmed)
            if (stepMatch != null) {
                val keyword = ScenarioStepKeyword.valueOf(stepMatch.groupValues[1].uppercase())
                currentStep = ScenarioStepBuilder(
                    keyword = keyword,
                    text = StringBuilder(stepMatch.groupValues[2].trim()),
                )
                stepBuilders += currentStep
            } else if (currentStep != null) {
                currentStep.appendContinuation(trimmed)
            }

            i++
        }

        return ParsedScenarioSteps(
            steps = stepBuilders
                .map { ScenarioStep(keyword = it.keyword, text = it.text.toString().trim()) }
                .filter { it.text.isNotBlank() },
            nextIndex = i,
        )
    }

    private fun isScenarioBoundary(trimmedLine: String): Boolean =
        REQUIREMENT_HEADER.matches(trimmedLine) ||
            SCENARIO_HEADER.matches(trimmedLine) ||
            MARKDOWN_HEADING.matches(trimmedLine)

    private data class ParsedScenarioSteps(
        val steps: List<ScenarioStep>,
        val nextIndex: Int,
    )

    private data class ScenarioStepBuilder(
        val keyword: ScenarioStepKeyword,
        val text: StringBuilder,
    ) {
        fun appendContinuation(line: String) {
            if (line.isBlank()) return
            if (text.isNotEmpty()) text.append('\n')
            text.append(line)
        }
    }

    private val REQUIREMENT_HEADER = Regex("^###\\s+Requirement:\\s*(.+)$")
    private val SCENARIO_HEADER = Regex("^####\\s+Scenario:\\s*(.+)$")
    private val SCENARIO_STEP_LINE = Regex(
        "^[-*]\\s*(?:\\*\\*)?(GIVEN|WHEN|THEN|AND)\\s*:?(?:\\*\\*)?\\s*:?\\s*(.*)$",
        RegexOption.IGNORE_CASE,
    )
    private val MARKDOWN_HEADING = Regex("^#{1,6}\\s+.+$")

    private fun String.slug(): String =
        lowercase().replace(Regex("[^a-z0-9а-я]+"), "-").trim('-')
}
