package org.rural.learn.data.sync
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.rural.learn.domain.sync.*
import java.io.IOException
/** No transport. Memory is deliberately lost on process restart. Every third call fails. */
class FakeSyncApi(private val latencyMs: Long=250,private val failEvery: Int=3): SyncApi {
 private val mutex=Mutex(); private var calls=0; private var remote=SyncSnapshot(); private val accepted=mutableSetOf<String>()
 override suspend fun exchange(ids: Set<String>,local: SyncSnapshot): SyncSnapshot {
  delay(latencyMs)
  return mutex.withLock { calls++; if(failEvery>0 && calls%failEvery==0) throw IOException("Simulated failure"); remote=MergePolicy.merge(remote,local); accepted.addAll(ids); remote }
 }
}
