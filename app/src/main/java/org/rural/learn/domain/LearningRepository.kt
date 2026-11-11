package org.rural.learn.domain
import kotlinx.coroutines.flow.Flow
import org.rural.learn.domain.gamification.RunState
import org.rural.learn.domain.content.Lesson
import org.rural.learn.domain.content.Subject
import kotlinx.serialization.Serializable
@Serializable data class Profile(val id: String, val name: String, val avatarId: Int, val languageCode: String, val createdAt: Long, val updatedAt: Long = createdAt)
@Serializable data class Progress(val learnerId: String, val lessonId: String, val stars: Int, val bestScore: Int, val attempts: Int, val completedAt: Long)
@Serializable data class Xp(val id: String, val learnerId: String, val amount: Int, val reason: String, val createdAt: Long)
@Serializable data class Badge(val learnerId: String, val badgeId: String, val awardedAt: Long)
@Serializable data class Day(val learnerId: String, val epochDay: Long)
@Serializable data class Event(val id: String, val learnerId: String, val type: String, val payloadJson: String, val ts: Long, val synced: Boolean = false)
data class Installation(val packId: String, val version: Int, val lang: String)
interface LearningRepository {
 val profiles: Flow<List<Profile>>; val progress: Flow<List<Progress>>; val xp: Flow<List<Xp>>; val badges: Flow<List<Badge>>; val days: Flow<List<Day>>; val events: Flow<List<Event>>; val installed: Flow<List<Installation>>
 suspend fun create(name: String, avatar: Int, lang: String): String
 suspend fun profileLanguage(learner: String, lang: String)
 suspend fun session(learner: String): RunState?
 suspend fun start(learner: String, lesson: Lesson, lang: String): RunState
 suspend fun save(learner: String, state: RunState)
 suspend fun answer(learner: String, state: RunState, lesson: Lesson, choice: String): RunState
 suspend fun finish(learner: String, state: RunState, lesson: Lesson, subjects: List<Subject>): RunState
 suspend fun feedback(learner: String,state: RunState,answers: List<Int>,clarity: Boolean): RunState
 suspend fun log(learner: String, type: String, payload: Map<String,String> = emptyMap())
}
