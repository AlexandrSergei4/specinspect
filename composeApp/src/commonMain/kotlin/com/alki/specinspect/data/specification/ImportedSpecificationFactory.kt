package com.alki.specinspect.data.specification

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.importer.parseGitHubRepository
import com.alki.specinspect.data.models.GitSource
import com.alki.specinspect.data.models.Specification
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
    ): Specification {
        val specName = name.trim()
        if (specName.isEmpty()) localizedError(AppTextKey.ErrorSpecNameRequired)
        if (files.isEmpty()) localizedError(AppTextKey.ErrorNoSpecFiles)

        val specSeed = Random.nextInt(100000, 999999)
        val subspecs = files
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
                    specKitSubspec
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
                    openSpecSubspec
                }
            }

        return Specification(
            id = "user-spec-$specSeed",
            name = specName,
            isDemo = false,
            subspecs = subspecs,
            gitSource = gitSource,
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
