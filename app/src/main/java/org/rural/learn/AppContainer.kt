package org.rural.learn
import android.app.Application
import androidx.room.Room
import org.rural.learn.data.local.GardenDatabase
import org.rural.learn.data.content.AssetContentRepository
import org.rural.learn.data.settings.SettingsStore
class AppContainer(app: Application) {
 val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()+kotlinx.coroutines.Dispatchers.IO)
 val database = Room.databaseBuilder(app,GardenDatabase::class.java,"garden.db").build()
 val learning: org.rural.learn.domain.LearningRepository = org.rural.learn.data.RoomLearningRepository(database)
 val syncApi: org.rural.learn.domain.sync.SyncApi = org.rural.learn.data.sync.FakeSyncApi()
 val sync: org.rural.learn.domain.sync.SyncScheduler = org.rural.learn.data.sync.WorkSyncScheduler(app)
 val settings: org.rural.learn.domain.Preferences = SettingsStore(app)
 val packs: org.rural.learn.domain.content.ContentPackSource = org.rural.learn.data.content.LocalPackSource(app,database.dao())
 val content: org.rural.learn.domain.content.ContentRepository = AssetContentRepository(app)
}
class GardenApp: Application() {
 val container: AppContainer by lazy { AppContainer(this) }
 override fun onCreate() {
  super.onCreate()
  if(BuildConfig.DEBUG) {
   android.os.StrictMode.setThreadPolicy(android.os.StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())
   android.os.StrictMode.setVmPolicy(android.os.StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build())
  }
 }
}
