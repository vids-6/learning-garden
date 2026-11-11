package org.rural.learn.data.content
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.rural.learn.domain.content.*
class AssetContentRepository(private val context: Context): ContentRepository {
 private val json = Json { ignoreUnknownKeys = false }
 override suspend fun packs(): List<Pack> = withContext(Dispatchers.IO) { json.decodeFromString<PackManifest>(context.assets.open("content/manifest.json").bufferedReader().use { it.readText() }).packs }
 override suspend fun subjects(lang: String): List<Subject> = withContext(Dispatchers.IO) {
  require(lang in listOf("en","hi","mr"))
  packs().filter { it.lang==lang && it.bundled }.map { pack -> json.decodeFromString<Subject>(context.assets.open(pack.path).bufferedReader().use { it.readText() }) }
 }
}
