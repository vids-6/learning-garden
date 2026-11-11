package org.rural.learn
import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.json.Json
import org.rural.learn.domain.content.PackManifest
import java.io.File
class SetupTest { @Test fun bundledManifestHasAllLanguages() { val manifest=Json.decodeFromString<PackManifest>(File("src/main/assets/content/manifest.json").readText()); assertEquals(setOf("en","hi","mr"),manifest.packs.map { it.lang }.toSet()) } }
