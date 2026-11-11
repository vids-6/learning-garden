package org.rural.learn.domain.gamification
import kotlinx.serialization.Serializable
import org.rural.learn.domain.content.Activity
@Serializable data class AnswerRecord(val activityId: String, val attempt: Int, val correct: Boolean, val resolved: Boolean, val xp: Int, val latencyMs: Long)
@Serializable data class RunState(val runId: String, val lessonId: String, val lang: String, val index: Int = 0, val records: List<AnswerRecord> = emptyList(), val feedback: Boolean = false, val cardsDone: Boolean = false, val complete: Boolean = false, val resultXp: Int = 0, val resultBadges: List<String> = emptyList(), val startedAt: Long, val questionStartedAt: Long = startedAt, val surveyDone: Boolean = false) {
 val correctCount get() = records.count { it.resolved && it.correct }
 val activityXp get() = records.sumOf { it.xp }
}
data class Level(val number: Int, val within: Int, val threshold: Int) { val stage get() = (number-1).coerceIn(0,5) }
object LearningRules {
 fun stars(percent: Int): Int { require(percent in 0..100); return if(percent>=90) 3 else if(percent>=70) 2 else 1 }
 fun percent(correct: Int, total: Int): Int { require(total>0 && correct in 0..total); return correct*100/total }
 fun correctXp(attempt: Int): Int { require(attempt in 1..2); return if(attempt==1) 10 else 5 }
 fun completionXp(percent: Int, previousBest: Int?): Int = 20 + (if(stars(percent)==3) 10 else 0) + (if(previousBest!=null && percent>previousBest) 10 else 0)
 fun level(total: Int): Level { require(total>=0); var remaining=total; var n=1; while(remaining>=100+50*(n-1)) { remaining-=100+50*(n-1); n++ }; return Level(n,remaining,100+50*(n-1)) }
 fun unlocked(index: Int, previousStars: Int): Boolean = index==0 || previousStars>=1
 fun answer(activity: Activity, choice: String): Boolean = when(activity.type) {
  "MCQ","LISTEN_PICK" -> choice.toIntOrNull()==activity.correctIndex
  "NUMBER_ENTRY" -> choice.toIntOrNull()==activity.answer
  "MATCH_PAIRS" -> choice==activity.pairs.map { it.id }.sorted().joinToString(",")
  else -> false
 }
 fun submit(state: RunState, activity: Activity, correct: Boolean, now: Long): RunState {
  require(!state.complete && !state.feedback)
  val previous=state.records.filter { it.activityId==activity.id }; require(previous.none { it.resolved } && previous.size<2)
  val attempt=previous.size+1
  val record=AnswerRecord(activity.id,attempt,correct,correct || attempt==2,if(correct) correctXp(attempt) else 0,(now-state.questionStartedAt).coerceAtLeast(0))
  return state.copy(records=state.records+record,feedback=true)
 }
 fun next(state: RunState, now: Long): RunState {
  require(state.feedback && !state.complete)
  return state.copy(index=state.index+(if(state.records.last().resolved) 1 else 0),feedback=false,questionStartedAt=now)
 }
}
data class BadgeFacts(val completedCount: Int = 0, val perfect: Boolean = false, val comeback: Boolean = false, val unitComplete: Boolean = false, val subjectCount: Int = 0, val practiceDays: Int = 0, val audioCount: Int = 0, val languageCount: Int = 0)
object BadgeRules {
 fun earned(f: BadgeFacts): Set<String> = buildSet {
  if(f.completedCount>=1) add("FIRST_STEPS")
  if(f.perfect) add("PERFECT_ROUND")
  if(f.comeback) add("COMEBACK")
  if(f.unitComplete) add("UNIT_MASTER")
  if(f.subjectCount>=3) add("EXPLORER")
  if(f.practiceDays>=3) add("PRACTICE_WEEK")
  if(f.audioCount>=10) add("LISTENER")
  if(f.languageCount>=2) add("BILINGUAL")
 }
}
