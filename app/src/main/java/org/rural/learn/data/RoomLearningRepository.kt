package org.rural.learn.data
import androidx.room.withTransaction
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.rural.learn.data.local.*
import org.rural.learn.domain.*
import org.rural.learn.domain.content.*
import org.rural.learn.domain.gamification.*
import java.util.UUID
import java.util.Calendar
import java.util.TimeZone
class RoomLearningRepository(private val db: GardenDatabase): LearningRepository {
 private val dao=db.dao(); private val json=Json
 override val profiles=dao.learners().map { list -> list.map { Profile(it.id,it.name,it.avatarId,it.languageCode,it.createdAt,it.updatedAt) } }
 override val progress=dao.progress().map { list -> list.map { Progress(it.learnerId,it.lessonId,it.stars,it.bestScore,it.attempts,it.completedAt) } }
 override val xp=dao.xp().map { list -> list.map { Xp(it.id,it.learnerId,it.amount,it.reason,it.createdAt) } }
 override val badges=dao.badges().map { list -> list.map { Badge(it.learnerId,it.badgeId,it.awardedAt) } }
 override val days=dao.days().map { list -> list.map { Day(it.learnerId,it.epochDay) } }
 override val events=dao.events().map { list -> list.map { Event(it.id,it.learnerId,it.type,it.payloadJson,it.ts,it.synced) } }
 override val installed=dao.installed().map { list -> list.map { Installation(it.packId,it.version,it.lang) } }
 override suspend fun create(name: String, avatar: Int, lang: String): String { require(name.isNotBlank()); val id=UUID.randomUUID().toString(); dao.saveLearner(Learner(id,name.trim().take(24),avatar,lang,System.currentTimeMillis())); log(id,"profile_created"); return id }
 override suspend fun profileLanguage(learner: String,lang: String) { db.withTransaction {
  val profile=dao.learners().first().find { it.id==learner } ?: return@withTransaction
  dao.saveLearner(profile.copy(languageCode=lang,updatedAt=System.currentTimeMillis()))
  log(learner,"language_selected",mapOf("lang" to lang))
 } }
 override suspend fun session(learner: String): RunState? = dao.session(learner)?.let { json.decodeFromString<RunState>(it.stateJson) }
 override suspend fun save(learner: String, state: RunState) { dao.saveSession(LessonSession(learner,state.lessonId,state.lang,json.encodeToString(state),System.currentTimeMillis())) }
 override suspend fun start(learner: String, lesson: Lesson, lang: String): RunState { val now=System.currentTimeMillis(); val state=RunState(UUID.randomUUID().toString(),lesson.id,lang,startedAt=now); db.withTransaction { save(learner,state); log(learner,"lesson_started",mapOf("lessonId" to lesson.id,"runId" to state.runId,"lang" to lang)) }; return state }
 override suspend fun answer(learner: String,state: RunState,lesson: Lesson,choice: String): RunState = db.withTransaction {
  val persisted=session(learner) ?: error("Missing session")
  require(persisted==state)
  val activity=lesson.activities[state.index]
  val next=LearningRules.submit(state,activity,LearningRules.answer(activity,choice),System.currentTimeMillis()); val record=next.records.last()
  save(learner,next)
  if(record.xp>0) dao.addXp(XpLedger(UUID.nameUUIDFromBytes("${state.runId}:${activity.id}".toByteArray()).toString(),learner,record.xp,"activity:${activity.id}",System.currentTimeMillis()))
  dao.addDay(PracticeDay(learner,today()))
  log(learner,"activity_answered",mapOf("lessonId" to lesson.id,"runId" to state.runId,"activityId" to activity.id,"correct" to record.correct.toString(),"attempt" to record.attempt.toString(),"latencyMs" to record.latencyMs.toString()))
  next
 }
 override suspend fun finish(learner: String,state: RunState,lesson: Lesson,subjects: List<Subject>): RunState = db.withTransaction {
  val persisted=session(learner) ?: error("Missing session")
  if(persisted.complete) return@withTransaction persisted
  require(persisted.runId==state.runId && state.index==lesson.activities.size)
  val now=System.currentTimeMillis(); val score=LearningRules.percent(state.correctCount,lesson.activities.size); val previous=dao.progressFor(learner,lesson.id)
  val bonus=LearningRules.completionXp(score,previous?.bestScore)
  dao.addXp(XpLedger(UUID.nameUUIDFromBytes("${state.runId}:completion".toByteArray()).toString(),learner,bonus,"lesson:${lesson.id}",now))
  dao.saveProgress(LessonProgress(learner,lesson.id,maxOf(previous?.stars ?: 0,LearningRules.stars(score)),maxOf(previous?.bestScore ?: 0,score),(previous?.attempts ?: 0)+1,now))
  dao.addDay(PracticeDay(learner,today()))
  log(learner,"lesson_completed",mapOf("lessonId" to lesson.id,"runId" to state.runId,"lang" to state.lang,"score" to score.toString(),"durationMs" to (now-state.startedAt).toString(),"repeat" to (previous!=null).toString()))
  val all=dao.progress().first().filter { it.learnerId==learner }; val logs=dao.events().first().filter { it.learnerId==learner }
  val subject=subjects.first { s -> s.units.any { u -> u.lessons.any { it.id==lesson.id } } }
  val languages=logs.filter { it.type=="lesson_completed" }.map { json.parseToJsonElement(it.payloadJson).jsonObject }.filter { it["lessonId"]?.jsonPrimitive?.content==lesson.id }.mapNotNull { it["lang"]?.jsonPrimitive?.content }.toSet()
  val facts=BadgeFacts(all.size,score==100,previous!=null,subject.units.flatMap { it.lessons }.all { l -> all.any { it.lessonId==l.id && it.stars>=1 } },subjects.count { s -> s.units.flatMap { it.lessons }.any { l -> all.any { it.lessonId==l.id } } },dao.days().first().filter { it.learnerId==learner && it.epochDay in weekStart()..today() }.size,logs.count { it.type=="audio_played" },languages.size)
  val old=dao.badges().first().filter { it.learnerId==learner }.map { it.badgeId }.toSet(); val earned=BadgeRules.earned(facts)-old
  earned.forEach { dao.addBadge(BadgeAward(learner,it,now)) }
  val result=state.copy(complete=true,resultXp=state.activityXp+bonus,resultBadges=earned.toList()); save(learner,result); result
 }
 override suspend fun feedback(learner: String,state: RunState,answers: List<Int>,clarity: Boolean): RunState = db.withTransaction {
  require(answers.size==3 && answers.all { it in 0..2 } && state.complete)
  val current=session(learner) ?: error("Missing session")
  require(current.runId==state.runId)
  if(current.surveyDone) return@withTransaction current
  answers.forEachIndexed { i,value -> log(learner,"survey_response",mapOf("lessonId" to state.lessonId,"runId" to state.runId,"question" to (i+1).toString(),"value" to value.toString())) }
  log(learner,"clarity_feedback",mapOf("lessonId" to state.lessonId,"thumb" to if(clarity) "up" else "down"))
  current.copy(surveyDone=true).also { save(learner,it) }
 }
 override suspend fun log(learner: String,type: String,payload: Map<String,String>) {
  val id=UUID.randomUUID().toString(); val now=System.currentTimeMillis(); val body=JsonObject(payload.mapValues { JsonPrimitive(it.value) }).toString()
  dao.event(AnalyticsEvent(id,learner,type,body,now))
  if(learner.isNotBlank() && type=="audio_played" && dao.events().first().count { it.learnerId==learner && it.type=="audio_played" }>=10) dao.addBadge(BadgeAward(learner,"LISTENER",now))
  dao.enqueue(SyncOutbox(id,"event",json.encodeToString(Event(id,learner,type,body,now)),now))
 }
 companion object {
  fun today(): Long { val now=System.currentTimeMillis(); return Math.floorDiv(now+TimeZone.getDefault().getOffset(now),86400000L) }
  fun weekStart(): Long { val c=Calendar.getInstance(); return today()-((c.get(Calendar.DAY_OF_WEEK)+5)%7) }
 }
}
