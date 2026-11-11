package org.rural.learn
import org.junit.Assert.*
import org.junit.Test
import org.rural.learn.domain.gamification.*
import org.rural.learn.domain.content.*
class LearningRulesTest {
 private val a=Activity("a","MCQ","?",options=listOf(Option("a"),Option("b")),correctIndex=1,explanation="b")
 private fun run()=RunState("run","lesson","en",startedAt=100)
 @Test fun starBoundaries() { listOf(0 to 1,69 to 1,70 to 2,89 to 2,90 to 3,100 to 3).forEach { assertEquals(it.second,LearningRules.stars(it.first)) } }
 @Test fun xpAndBestBoundaries() { assertEquals(10,LearningRules.correctXp(1)); assertEquals(5,LearningRules.correctXp(2)); assertEquals(30,LearningRules.completionXp(100,null)); assertEquals(40,LearningRules.completionXp(100,80)); assertEquals(30,LearningRules.completionXp(100,100)); assertEquals(20,LearningRules.completionXp(80,90)) }
 @Test fun levelsUseIncrementalThresholds() { assertEquals(Level(1,99,100),LearningRules.level(99)); assertEquals(Level(2,0,150),LearningRules.level(100)); assertEquals(Level(3,0,200),LearningRules.level(250)); assertEquals(5,LearningRules.level(5000).stage) }
 @Test fun unlock() { assertTrue(LearningRules.unlocked(0,0)); assertFalse(LearningRules.unlocked(1,0)); assertTrue(LearningRules.unlocked(1,1)) }
 @Test fun twoWrongAnswersResolveAndReveal() { val first=LearningRules.submit(run(),a,false,200); assertFalse(first.records.last().resolved); val second=LearningRules.submit(LearningRules.next(first,300),a,false,400); assertTrue(second.records.last().resolved); assertEquals(0,second.activityXp); assertEquals(1,LearningRules.next(second,500).index) }
 @Test fun retryGetsFiveAndCredit() { val first=LearningRules.submit(run(),a,false,200); val second=LearningRules.submit(LearningRules.next(first,300),a,true,400); assertEquals(5,second.activityXp); assertEquals(1,second.correctCount); assertEquals(2,second.records.last().attempt) }
 @Test(expected=IllegalArgumentException::class) fun cannotSubmitDuringFeedback() { LearningRules.submit(LearningRules.submit(run(),a,true,200),a,true,300) }
 @Test fun firstTryGetsTenAndLatency() { val s=LearningRules.submit(run(),a,true,175); assertEquals(10,s.activityXp); assertEquals(75L,s.records.last().latencyMs) }
 @Test fun answerTypes() { assertTrue(LearningRules.answer(a,"1")); assertFalse(LearningRules.answer(a,"0")); assertTrue(LearningRules.answer(a.copy(type="LISTEN_PICK"),"1")); assertTrue(LearningRules.answer(a.copy(type="NUMBER_ENTRY",answer=42),"42")); assertFalse(LearningRules.answer(a.copy(type="NUMBER_ENTRY",answer=42),"")); assertTrue(LearningRules.answer(a.copy(type="MATCH_PAIRS",pairs=listOf(PairItem("b","x","y"),PairItem("a","q","r"))),"a,b")) }
 @Test fun allBadgesAndNoPrematureBadges() { assertTrue(BadgeRules.earned(BadgeFacts()).isEmpty()); assertEquals(8,BadgeRules.earned(BadgeFacts(1,true,true,true,3,3,10,2)).size); assertFalse("PRACTICE_WEEK" in BadgeRules.earned(BadgeFacts(practiceDays=2))); assertFalse("LISTENER" in BadgeRules.earned(BadgeFacts(audioCount=9))) }
 @Test fun percent() { assertEquals(80,LearningRules.percent(4,5)) }
 @Test fun serializationRestoresFeedback() { val state=LearningRules.submit(run(),a,false,200); assertEquals(state,kotlinx.serialization.json.Json.decodeFromString<RunState>(kotlinx.serialization.json.Json.encodeToString(state))) }
}
