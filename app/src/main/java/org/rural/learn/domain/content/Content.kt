package org.rural.learn.domain.content
import kotlinx.serialization.Serializable
@Serializable data class PackManifest(val packs: List<Pack>)
@Serializable data class Pack(val id: String, val lang: String, val subject: String, val version: Int, val sizeBytes: Long, val sha256: String, val path: String, val bundled: Boolean)
@Serializable data class Subject(val subjectId: String, val version: Int, val lang: String, val title: String, val icon: String, val units: List<LearningUnit>)
@Serializable data class LearningUnit(val id: String, val title: String, val lessons: List<Lesson>)
@Serializable data class Lesson(val id: String, val objective: String, val reviewed: Boolean = false, val cards: List<LearningCard>, val activities: List<Activity>)
@Serializable data class LearningCard(val id: String, val text: String, val image: String? = null, val audio: String? = null)
@Serializable data class Option(val text: String, val image: String? = null)
@Serializable data class PairItem(val id: String, val left: String, val right: String)
@Serializable data class Activity(val id: String, val type: String, val prompt: String, val promptAudio: String? = null, val options: List<Option> = emptyList(), val correctIndex: Int? = null, val explanation: String, val answer: Int? = null, val pairs: List<PairItem> = emptyList())
interface ContentRepository { suspend fun subjects(lang: String): List<Subject>; suspend fun packs(): List<Pack> }
