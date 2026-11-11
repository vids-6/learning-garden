package org.rural.learn.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity data class Learner(@PrimaryKey val id: String, val name: String, val avatarId: Int, val languageCode: String, val createdAt: Long, val updatedAt: Long = createdAt)
@Entity(primaryKeys=["learnerId","lessonId"]) data class LessonProgress(val learnerId: String, val lessonId: String, val stars: Int, val bestScore: Int, val attempts: Int, val completedAt: Long)
@Entity data class XpLedger(@PrimaryKey val id: String, val learnerId: String, val amount: Int, val reason: String, val createdAt: Long)
@Entity(primaryKeys=["learnerId","badgeId"]) data class BadgeAward(val learnerId: String, val badgeId: String, val awardedAt: Long)
@Entity(primaryKeys=["learnerId","epochDay"]) data class PracticeDay(val learnerId: String, val epochDay: Long)
@Entity data class AnalyticsEvent(@PrimaryKey val id: String, val learnerId: String, val type: String, val payloadJson: String, val ts: Long, val synced: Boolean = false)
@Entity data class SyncOutbox(@PrimaryKey val id: String, val entityType: String, val payloadJson: String, val createdAt: Long, val attempts: Int = 0)
@Entity data class InstalledPack(@PrimaryKey val packId: String, val version: Int, val lang: String)
@Entity data class LessonSession(@PrimaryKey val learnerId: String, val lessonId: String, val lang: String, val stateJson: String, val updatedAt: Long)
@Dao interface GardenDao {
 @Query("SELECT * FROM Learner ORDER BY createdAt") fun learners(): Flow<List<Learner>>
 @Upsert suspend fun saveLearner(value: Learner)
 @Query("SELECT * FROM LessonProgress") fun progress(): Flow<List<LessonProgress>>
 @Query("SELECT * FROM LessonProgress WHERE learnerId=:learner AND lessonId=:lesson") suspend fun progressFor(learner: String, lesson: String): LessonProgress?
 @Upsert suspend fun saveProgress(value: LessonProgress)
 @Query("SELECT * FROM XpLedger") fun xp(): Flow<List<XpLedger>>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun addXp(value: XpLedger)
 @Query("SELECT * FROM BadgeAward") fun badges(): Flow<List<BadgeAward>>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun addBadge(value: BadgeAward)
 @Query("SELECT * FROM PracticeDay") fun days(): Flow<List<PracticeDay>>
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun addDay(value: PracticeDay)
 @Query("SELECT * FROM LessonSession WHERE learnerId=:learner") suspend fun session(learner: String): LessonSession?
 @Upsert suspend fun saveSession(value: LessonSession)
 @Query("DELETE FROM LessonSession WHERE learnerId=:learner") suspend fun clearSession(learner: String)
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun event(value: AnalyticsEvent)
 @Query("SELECT * FROM AnalyticsEvent ORDER BY ts") fun events(): Flow<List<AnalyticsEvent>>
 @Query("SELECT * FROM AnalyticsEvent WHERE synced=0 ORDER BY ts LIMIT 100") suspend fun pendingEvents(): List<AnalyticsEvent>
 @Query("UPDATE AnalyticsEvent SET synced=1 WHERE id IN (:ids)") suspend fun markSynced(ids: List<String>)
 @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun enqueue(value: SyncOutbox)
 @Query("SELECT * FROM SyncOutbox ORDER BY createdAt LIMIT 100") suspend fun outbox(): List<SyncOutbox>
 @Query("DELETE FROM SyncOutbox WHERE id IN (:ids)") suspend fun acknowledge(ids: List<String>)
 @Query("UPDATE SyncOutbox SET attempts=attempts+1 WHERE id IN (:ids)") suspend fun failed(ids: List<String>)
 @Query("SELECT * FROM InstalledPack") fun installed(): Flow<List<InstalledPack>>
 @Upsert suspend fun install(value: InstalledPack)
}
@Database(entities=[Learner::class,LessonProgress::class,XpLedger::class,BadgeAward::class,PracticeDay::class,AnalyticsEvent::class,SyncOutbox::class,InstalledPack::class,LessonSession::class],version=1,exportSchema=true)
abstract class GardenDatabase: RoomDatabase() { abstract fun dao(): GardenDao }
