package org.rural.learn.domain.sync
import org.rural.learn.domain.*
import kotlinx.serialization.Serializable
@Serializable data class SyncSnapshot(val profiles: List<Profile> = emptyList(), val progress: List<Progress> = emptyList(), val xp: List<Xp> = emptyList(), val badges: List<Badge> = emptyList(),val days: List<Day> = emptyList(),val events: List<Event> = emptyList())
interface SyncApi { suspend fun exchange(ids: Set<String>,local: SyncSnapshot): SyncSnapshot }
interface SyncScheduler { fun schedule() }
object MergePolicy {
 fun merge(local: SyncSnapshot,remote: SyncSnapshot): SyncSnapshot = SyncSnapshot(
  profiles=(remote.profiles+local.profiles).groupBy { it.id }.values.map { list -> list.maxBy { it.updatedAt } },
  progress=(remote.progress+local.progress).groupBy { it.learnerId to it.lessonId }.values.map { list -> val newest=list.maxBy { it.completedAt }; newest.copy(stars=list.maxOf { it.stars },bestScore=list.maxOf { it.bestScore },attempts=list.maxOf { it.attempts }) },
  xp=(local.xp+remote.xp).distinctBy { it.id },
  badges=(local.badges+remote.badges).distinctBy { it.learnerId to it.badgeId },
  days=(local.days+remote.days).distinctBy { it.learnerId to it.epochDay },
  events=(local.events+remote.events).distinctBy { it.id }
 )
}
