package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioSource
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
 * - **WHEN** <текст>
 * - **THEN** <текст>
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
                        i++
                        var whenText = ""
                        var thenText = ""
                        while (i < lines.size) {
                            val sl = lines[i]
                            val ts = sl.trim()
                            if (REQUIREMENT_HEADER.matches(ts) || SCENARIO_HEADER.matches(ts)) break
                            val whenMatch = WHEN_LINE.matchEntire(ts)
                            val thenMatch = THEN_LINE.matchEntire(ts)
                            when {
                                whenMatch != null -> whenText = whenMatch.groupValues[1].trim()
                                thenMatch != null -> thenText = thenMatch.groupValues[1].trim()
                            }
                            i++
                        }
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

    private val REQUIREMENT_HEADER = Regex("^###\\s+Requirement:\\s*(.+)$")
    private val SCENARIO_HEADER = Regex("^####\\s+Scenario:\\s*(.+)$")
    private val WHEN_LINE = Regex("^[-*]\\s*\\*\\*WHEN\\*\\*\\s*(.+)$", RegexOption.IGNORE_CASE)
    private val THEN_LINE = Regex("^[-*]\\s*\\*\\*THEN\\*\\*\\s*(.+)$", RegexOption.IGNORE_CASE)

    private fun String.slug(): String =
        lowercase().replace(Regex("[^a-z0-9а-я]+"), "-").trim('-')
}
