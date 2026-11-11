package org.rural.learn
import org.junit.Assert.*
import org.junit.Test
import org.rural.learn.domain.content.*
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
class ContentIntegrityTest {
 private val root=File("src/main/assets")
 private val manifest=Json.decodeFromString<PackManifest>(File(root,"content/manifest.json").readText())
 private fun subjects(lang: String)=manifest.packs.filter { it.bundled && it.lang==lang }.associate { it.subject to Json.decodeFromString<Subject>(File(root,it.path).readText()) }
 private fun signature(s: Subject)=s.units.flatMap { it.lessons }.associate { l -> l.id to l.activities.associate { a -> a.id to listOf(a.type,a.correctIndex,a.answer,a.options.size,a.pairs.map { it.id }) } }
 @Test fun idsAndAnswersIdenticalAcrossLanguages() {
  val en=subjects("en"); assertEquals(setOf("math","vocab","lifeskills"),en.keys)
  listOf("hi","mr").forEach { lang -> val other=subjects(lang); assertEquals(en.keys,other.keys); en.forEach { (key,s) -> assertEquals("Language $lang subject $key",signature(s),signature(other.getValue(key))) } }
 }
 @Test fun manifestChecksumsAndSizes() { manifest.packs.forEach { p -> val bytes=File(root,p.path).readBytes(); assertEquals(p.sizeBytes,bytes.size.toLong()); assertEquals(p.sha256,MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }) } }
 @Test fun seedShapeAndAnswerValidity() { listOf("en","hi","mr").forEach { lang -> subjects(lang).values.forEach { s -> assertEquals(1,s.units.size); assertEquals(2,s.units.single().lessons.size); s.units.single().lessons.forEach { l -> assertFalse(l.reviewed); assertTrue(l.cards.isNotEmpty()); assertEquals(5,l.activities.size); assertEquals(5,l.activities.map { it.id }.toSet().size); l.activities.forEach { a -> assertTrue(a.prompt.isNotBlank()); assertTrue(a.explanation.isNotBlank()); when(a.type) { "MCQ","LISTEN_PICK" -> assertTrue(a.correctIndex in a.options.indices); "MATCH_PAIRS" -> assertEquals(a.pairs.size,a.pairs.map { it.id }.toSet().size); "NUMBER_ENTRY" -> assertNotNull(a.answer); else -> fail("Unknown type") } } } } } }
}
