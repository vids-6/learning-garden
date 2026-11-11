package org.rural.learn.domain
import kotlinx.coroutines.flow.Flow
data class Settings(val language: String = "", val audioOn: Boolean = true, val textScale: Float = 1f, val reducedMotion: Boolean = false, val dataSaver: Boolean = true, val activeLearnerId: String = "", val syncStatus: String = "idle")
interface Preferences {
 val flow: Flow<Settings>
 suspend fun language(v: String)
 suspend fun learner(v: String)
 suspend fun boolean(key: String,v: Boolean)
 suspend fun scale(v: Float)
 suspend fun syncStatus(v: String)
}
