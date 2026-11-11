package org.rural.learn.domain.analytics
import org.rural.learn.domain.Event
import kotlinx.serialization.json.*
data class ResearchMetrics(val completionPercent: Int, val averageSeconds: Long, val navigationErrors: Int, val firstScore: Int, val repeatScore: Int, val surveyAverage: Float)
object Research {
 private fun fields(e: Event): Map<String,String> = runCatching { Json.parseToJsonElement(e.payloadJson).jsonObject.mapValues { it.value.jsonPrimitive.content } }.getOrDefault(emptyMap())
 fun metrics(events: List<Event>): ResearchMetrics {
  val starts=events.filter { it.type=="lesson_started" }.mapNotNull { fields(it)["runId"] }.toSet()
  val ends=events.filter { it.type=="lesson_completed" }.distinctBy { fields(it)["runId"] ?: it.id }
  fun score(repeat: Boolean): Int { val scores=ends.filter { fields(it)["repeat"]==repeat.toString() }.mapNotNull { fields(it)["score"]?.toIntOrNull() }; return if(scores.isEmpty()) 0 else scores.average().toInt() }
  val durations=ends.mapNotNull { fields(it)["durationMs"]?.toLongOrNull() }
  val surveys=events.filter { it.type=="survey_response" }.mapNotNull { fields(it)["value"]?.toIntOrNull() }
  val completed=ends.mapNotNull { fields(it)["runId"] }.toSet().intersect(starts)
  return ResearchMetrics(if(starts.isEmpty()) 0 else completed.size*100/starts.size,if(durations.isEmpty()) 0 else (durations.average()/1000).toLong(),events.count { it.type=="wrong_tap" },score(false),score(true),if(surveys.isEmpty()) 0f else surveys.average().toFloat())
 }
 private fun csvCell(v: String): String { val safe=if(v.firstOrNull() in listOf('=','+','-','@','\t','\r')) "'"+v else v; return "\""+safe.replace("\"","\"\"")+"\"" }
 fun csv(events: List<Event>): String = "id,learnerId,type,payloadJson,ts,synced\r\n"+events.joinToString("\r\n",postfix=if(events.isEmpty()) "" else "\r\n") { e -> listOf(e.id,e.learnerId,e.type,e.payloadJson,e.ts.toString(),e.synced.toString()).joinToString(",") { csvCell(it) } }
}
