package com.alki.specinspect.data.specification

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.analytics.AnalyticsSpecificationType
import com.alki.specinspect.data.importer.parseGitHubRepository
import com.alki.specinspect.data.models.GitSource
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.Subspec
import com.alki.specinspect.data.openspec.OpenSpecParser
import com.alki.specinspect.data.speckit.SpecKitParser
import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.localizedError
import kotlin.random.Random

object ImportedSpecificationFactory {

    fun create(
        name: String,
        files: List<ImportedSpecFile>,
        gitSource: GitSource? = null,
    ): Specification = createWithMetadata(
        name = name,
        files = files,
        gitSource = gitSource,
    ).specification

    fun createWithMetadata(
        name: String,
        files: List<ImportedSpecFile>,
        gitSource: GitSource? = null,
    ): ImportedSpecificationResult {
        val specName = name.trim()
        if (specName.isEmpty()) localizedError(AppTextKey.ErrorSpecNameRequired)
        if (files.isEmpty()) localizedError(AppTextKey.ErrorNoSpecFiles)

        val specSeed = Random.nextInt(100000, 999999)
        val parsedSubspecs = files
            .sortedBy { it.name.lowercase() }
            .mapIndexed { index, file ->
                val subspecName = file.name.trim()
                if (subspecName.isEmpty()) localizedError(AppTextKey.ErrorSubspecNameMissing)

                val idPrefix = "user-$specSeed-$index"
                val specKitSubspec = SpecKitParser.parseSubspec(
                    name = subspecName,
                    content = file.content,
                    idPrefix = idPrefix,
                    filePath = file.path,
                )
                if (specKitSubspec.requirements.isNotEmpty()) {
                    ParsedImportedSubspec(
                        subspec = specKitSubspec,
                        type = AnalyticsSpecificationType.SpecKit,
                    )
                } else {
                    val openSpecSubspec = OpenSpecParser.parseSubspec(
                        name = subspecName,
                        content = file.content,
                        idPrefix = idPrefix,
                        filePath = file.path,
                    )
                    if (openSpecSubspec.requirements.isEmpty()) {
                        localizedError(AppTextKey.ErrorSpecKitOrRequirementParseFailed, subspecName)
                    }
                    ParsedImportedSubspec(
                        subspec = openSpecSubspec,
                        type = AnalyticsSpecificationType.OpenSpec,
                    )
                }
            }

        val subspecs = parsedSubspecs.map { it.subspec }
        return ImportedSpecificationResult(
            specification = Specification(
                id = "user-spec-$specSeed",
                name = specName,
                isDemo = false,
                subspecs = subspecs,
                gitSource = gitSource,
            ),
            type = parsedSubspecs.map { it.type }.toSpecificationType(),
        )
    }

    fun gitSourceFrom(repositoryUrl: String, branch: String): GitSource {
        val repository = parseGitHubRepository(repositoryUrl)
        return GitSource(
            repository = "${repository.owner}/${repository.repo}",
            branch = branch.trim(),
        )
    }
}

data class ImportedSpecificationResult(
    val specification: Specification,
    val type: AnalyticsSpecificationType,
)

private data class ParsedImportedSubspec(
    val subspec: Subspec,
    val type: AnalyticsSpecificationType,
)

private fun List<AnalyticsSpecificationType>.toSpecificationType(): AnalyticsSpecificationType {
    val uniqueTypes = distinct()
    return if (uniqueTypes.size == 1) uniqueTypes.single() else AnalyticsSpecificationType.Mixed
}
