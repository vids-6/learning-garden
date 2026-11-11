package org.rural.learn.data.content
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.rural.learn.data.local.*
import org.rural.learn.domain.content.*
import java.io.File
class LocalPackSource(private val context: Context,private val dao: GardenDao): ContentPackSource {
 override suspend fun install(pack: Pack): Unit = withContext(Dispatchers.IO) {
  require(pack.id.matches(Regex("[a-z_]+")))
  require(pack.path.startsWith("content/") && !pack.path.contains(".."))
  val bytes=context.assets.open(pack.path).use { it.readBytes() }
  check(PackVerification.verify(bytes,pack)) { "Checksum mismatch" }
  val folder=File(context.filesDir,"packs").apply { mkdirs() }; val temp=File(folder,"${pack.id}.tmp"); val target=File(folder,"${pack.id}.json")
  temp.outputStream().use { it.write(bytes); it.fd.sync() }
  check(temp.renameTo(target)) { "Pack install failed" }
  dao.install(InstalledPack(pack.id,pack.version,pack.lang))
 }
 override suspend fun storageBytes(): Long=withContext(Dispatchers.IO) { sequenceOf(context.filesDir,File(context.applicationInfo.dataDir,"databases")).flatMap { it.walkTopDown().filter { f -> f.isFile }.asSequence() }.sumOf { it.length() } }
}
