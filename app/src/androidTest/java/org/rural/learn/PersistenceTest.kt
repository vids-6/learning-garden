package org.rural.learn
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.rural.learn.data.local.GardenDatabase
import org.rural.learn.data.RoomLearningRepository
import org.rural.learn.data.content.AssetContentRepository
import org.rural.learn.domain.gamification.LearningRules
@RunWith(AndroidJUnit4::class)
class PersistenceTest {
 @Test fun everyAnswerAndCompletionSurviveDatabaseReopen()=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext; val name="persistence-test.db"; context.deleteDatabase(name)
  var db=Room.databaseBuilder(context,GardenDatabase::class.java,name).build(); var repo=RoomLearningRepository(db)
  val subjects=AssetContentRepository(context).subjects("en"); val lesson=subjects.first { it.subjectId=="math" }.units.single().lessons.first()
  val id=repo.create("Test",0,"en"); var state=repo.start(id,lesson,"en"); state=state.copy(cardsDone=true); repo.save(id,state)
  state=repo.answer(id,state,lesson,"0"); assertTrue(state.feedback); assertFalse(state.records.last().resolved)
  db.close(); db=Room.databaseBuilder(context,GardenDatabase::class.java,name).build(); repo=RoomLearningRepository(db)
  assertEquals(state,repo.session(id)); state=LearningRules.next(repo.session(id)!!,System.currentTimeMillis()); repo.save(id,state)
  state=repo.answer(id,state,lesson,"1"); assertEquals(5,state.activityXp)
  state=LearningRules.next(state,System.currentTimeMillis()); repo.save(id,state)
  while(state.index<lesson.activities.size) {
   val a=lesson.activities[state.index]; val answer=when(a.type) { "NUMBER_ENTRY"->a.answer.toString(); "MATCH_PAIRS"->a.pairs.map { it.id }.sorted().joinToString(","); else->a.correctIndex.toString() }
   state=repo.answer(id,state,lesson,answer); state=LearningRules.next(state,System.currentTimeMillis());repo.save(id,state)
  }
  state=repo.finish(id,state,lesson,subjects); assertTrue(state.complete); assertEquals(75,state.resultXp)
  val total=repo.xp.first().sumOf { it.amount }; assertEquals(75,total)
  assertEquals(state,repo.finish(id,state,lesson,subjects)); assertEquals(total,repo.xp.first().sumOf { it.amount })
  state=repo.feedback(id,state,listOf(2,1,2),true);repo.feedback(id,state,listOf(2,1,2),true)
  assertEquals(3,repo.events.first().count { it.type=="survey_response" })
  db.close();db=Room.databaseBuilder(context,GardenDatabase::class.java,name).build();repo=RoomLearningRepository(db)
  assertTrue(repo.session(id)!!.surveyDone);assertEquals(3,repo.progress.first().single().stars);db.close();context.deleteDatabase(name);Unit
 }
 @Test fun offlineAllLanguagesLoadAndChecksumInstall()=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext; val source=AssetContentRepository(context)
  listOf("en","hi","mr").forEach { lang -> assertEquals(6,source.subjects(lang).flatMap { it.units }.flatMap { it.lessons }.size) }
  val name="pack-test.db";context.deleteDatabase(name)
  val db=Room.databaseBuilder(context,GardenDatabase::class.java,name).build()
  val installer=org.rural.learn.data.content.LocalPackSource(context,db.dao())
  val demo=source.packs().first { !it.bundled }.copy(id="verification_pack")
  installer.install(demo); assertTrue(db.dao().installed().first().any { it.packId==demo.id })
  try { installer.install(demo.copy(id="corrupt_pack",sha256="0".repeat(64))); fail("Corrupt pack accepted") } catch(_: IllegalStateException) {}
  assertFalse(db.dao().installed().first().any { it.packId=="corrupt_pack" })
  db.close();context.deleteDatabase(name);java.io.File(context.filesDir,"packs/verification_pack.json").delete();Unit
 }
}
