package org.rural.learn.data.sync
import android.content.Context
import androidx.work.*
import androidx.room.withTransaction
import org.rural.learn.GardenApp
import org.rural.learn.data.local.*
import org.rural.learn.domain.sync.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
class WorkSyncScheduler(private val context: Context): SyncScheduler {
 override fun schedule() {
  WorkManager.getInstance(context).enqueueUniqueWork("garden-sync",ExistingWorkPolicy.KEEP,OneTimeWorkRequestBuilder<GardenSyncWorker>().setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build())
 }
}
class GardenSyncWorker(context: Context,params: WorkerParameters): CoroutineWorker(context,params) {
 override suspend fun doWork(): Result {
  val c=(applicationContext as GardenApp).container; val dao=c.database.dao(); val learner=c.settings.flow.first().activeLearnerId
  c.settings.syncStatus("running"); c.learning.log(learner,"sync_attempt",mapOf("attempt" to runAttemptCount.toString()))
  return try {
   var batch=dao.outbox()
   while(batch.isNotEmpty()) {
    val repo=c.learning
    val local=SyncSnapshot(repo.profiles.first(),repo.progress.first(),repo.xp.first(),repo.badges.first(),repo.days.first(),repo.events.first())
    val remote=c.syncApi.exchange(batch.map { it.id }.toSet(),local)
    c.database.withTransaction {
     // Re-read inside the transaction so newer local learning always participates.
     val current=SyncSnapshot(repo.profiles.first(),repo.progress.first(),repo.xp.first(),repo.badges.first(),repo.days.first(),repo.events.first())
     val merged=MergePolicy.merge(current,remote)
     merged.profiles.forEach { dao.saveLearner(Learner(it.id,it.name,it.avatarId,it.languageCode,it.createdAt,it.updatedAt)) }
     merged.progress.forEach { dao.saveProgress(LessonProgress(it.learnerId,it.lessonId,it.stars,it.bestScore,it.attempts,it.completedAt)) }
     merged.xp.forEach { dao.addXp(XpLedger(it.id,it.learnerId,it.amount,it.reason,it.createdAt)) }
     merged.badges.forEach { dao.addBadge(BadgeAward(it.learnerId,it.badgeId,it.awardedAt)) }
     merged.days.forEach { dao.addDay(PracticeDay(it.learnerId,it.epochDay)) }
     merged.events.forEach { dao.event(AnalyticsEvent(it.id,it.learnerId,it.type,it.payloadJson,it.ts,it.synced)) }
     dao.markSynced(batch.filter { it.entityType=="event" }.map { it.id }); dao.acknowledge(batch.map { it.id })
    }
    batch=dao.outbox()
   }
   c.settings.syncStatus("success"); c.learning.log(learner,"sync_result",mapOf("success" to "true")); Result.success()
  } catch(_: Exception) {
   dao.failed(dao.outbox().map { it.id }); c.settings.syncStatus("failed"); c.learning.log(learner,"sync_result",mapOf("success" to "false")); Result.retry()
  }
 }
}
