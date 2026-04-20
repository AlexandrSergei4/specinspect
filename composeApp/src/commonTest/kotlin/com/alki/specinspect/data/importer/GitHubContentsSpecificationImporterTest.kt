package com.alki.specinspect.data.importer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GitHubContentsSpecificationImporterTest {

    @Test
    fun parsesOwnerAndRepoFromShortFormat() {
        val repository = parseGitHubRepository("octo-org/spec-repo")

        assertEquals("octo-org", repository.owner)
        assertEquals("spec-repo", repository.repo)
    }

    @Test
    fun parsesOwnerAndRepoFromGitHubSshUrl() {
        val repository = parseGitHubRepository("git@github.com:octo-org/spec-repo.git")

        assertEquals("octo-org", repository.owner)
        assertEquals("spec-repo", repository.repo)
    }

    @Test
    fun normalizesSpecificationPathAndSpecNames() {
        assertEquals("openspec/specs", normalizeSpecificationPath("/openspec/specs/"))
        assertEquals("dashboard", resolveImportedSpecName("openspec/specs", "openspec/specs/dashboard/spec.md"))
        assertEquals("mobile/feed", resolveImportedSpecName("openspec/specs", "openspec/specs/mobile/feed/spec.md"))
    }

    @Test
    fun rejectsNonGithubUrls() {
        assertFailsWith<IllegalStateException> {
            parseGitHubRepository("git@gitlab.com:octo-org/spec-repo.git")
        }
    }
}
