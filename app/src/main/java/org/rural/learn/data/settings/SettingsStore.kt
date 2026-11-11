package org.rural.learn.data.settings
import android.content.Context
import org.rural.learn.domain.Settings
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
private val Context.settings by preferencesDataStore("garden_settings")
class SettingsStore(context: Context): org.rural.learn.domain.Preferences {
 private val store = context.settings
 override val flow = store.data.map { Settings(it[stringPreferencesKey("language")] ?: "", it[booleanPreferencesKey("audioOn")] ?: true, it[floatPreferencesKey("textScale")] ?: 1f, it[booleanPreferencesKey("reducedMotion")] ?: false, it[booleanPreferencesKey("dataSaver")] ?: true, it[stringPreferencesKey("activeLearnerId")] ?: "", it[stringPreferencesKey("syncStatus")] ?: "idle") }
 override suspend fun language(v: String) { store.edit { it[stringPreferencesKey("language")] = v } }
 override suspend fun learner(v: String) { store.edit { it[stringPreferencesKey("activeLearnerId")] = v } }
 override suspend fun boolean(key: String, v: Boolean) { require(key in listOf("audioOn","reducedMotion","dataSaver")); store.edit { it[booleanPreferencesKey(key)] = v } }
 override suspend fun scale(v: Float) { store.edit { it[floatPreferencesKey("textScale")] = v.coerceIn(1f,1.5f) } }
 override suspend fun syncStatus(v: String) { store.edit { it[stringPreferencesKey("syncStatus")] = v } }
}
