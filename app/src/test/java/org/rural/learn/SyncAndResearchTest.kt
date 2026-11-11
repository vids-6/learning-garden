package org.rural.learn
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.rural.learn.domain.*
import org.rural.learn.domain.sync.*
import org.rural.learn.domain.analytics.*
import org.rural.learn.data.sync.FakeSyncApi
class SyncAndResearchTest {
 @Test fun mergeUnionAndMaxNeverLosesNewerProgress() { val local=SyncSnapshot(profiles=listOf(Profile("p","New",0,"mr",0,50)),progress=listOf(Progress("p","l",2,80,4,500)),xp=listOf(Xp("x","p",10,"a",0)),badges=listOf(Badge("p","B",0))); val remote=SyncSnapshot(profiles=listOf(Profile("p","Old",1,"en",0,20)),progress=listOf(Progress("p","l",3,100,2,100)),xp=local.xp+Xp("y","p",5,"b",0),badges=local.badges+Badge("p","C",0)); val m=MergePolicy.merge(local,remote); assertEquals("New",m.profiles.single().name); assertEquals(3,m.progress.single().stars); assertEquals(100,m.progress.single().bestScore); assertEquals(500L,m.progress.single().completedAt); assertEquals(4,m.progress.single().attempts); assertEquals(15,m.xp.sumOf { it.amount }); assertEquals(2,m.badges.size); assertEquals(m,MergePolicy.merge(m,remote)) }
 @Test fun fakeIsIdempotentAndCanFail() = runTest { val api=FakeSyncApi(1,3); val s=SyncSnapshot(xp=listOf(Xp("x","p",10,"a",0))); api.exchange(setOf("uuid"),s); assertEquals(10,api.exchange(setOf("uuid"),s).xp.sumOf { it.amount }); try { api.exchange(setOf("uuid"),s); fail("Must fail") } catch(_: java.io.IOException) {} }
 private fun e(id: String,type: String,payload: String)=Event(id,"p",type,payload,1)
 @Test fun metricsDistinguishRepeatAndCompletion() { val events=listOf(e("1","lesson_started","{\"runId\":\"a\"}"),e("2","lesson_started","{\"runId\":\"b\"}"),e("3","lesson_completed","{\"runId\":\"a\",\"durationMs\":\"5000\",\"score\":\"80\",\"repeat\":\"false\"}"),e("4","wrong_tap","{}"),e("5","survey_response","{\"value\":\"2\"}")); assertEquals(ResearchMetrics(50,5,1,80,0,2f),Research.metrics(events)); assertEquals(0,Research.metrics(emptyList()).completionPercent) }
 @Test fun csvEscapesQuotesNewlinesAndFormulas() { val csv=Research.csv(listOf(e("=1","survey_response","a,\"b\"\n"))); assertTrue(csv.contains("\"'=1\"")); assertTrue(csv.contains("a,\"\"b\"\"\n")); assertTrue(csv.endsWith("\r\n")) }
}
