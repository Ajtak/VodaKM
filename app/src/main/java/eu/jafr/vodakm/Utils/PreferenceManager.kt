package eu.jafr.vodakm.Utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager private constructor(context: Context) {

    private val appContext = context.applicationContext // Use application context

    private val Context.dataStore by preferencesDataStore("user_preferences")

    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context)
                INSTANCE = instance
                instance
            }
        }

        val START_NEAREST_POINT_ID = intPreferencesKey("river_start_nearest_point")
        val END_NEAREST_POINT_ID = intPreferencesKey("river_end_nearest_point")
    }

    suspend fun saveNearestPoint(point: Int, keyName: Preferences.Key<Int>) {
        appContext.dataStore.edit { settings ->
            settings[keyName] = point
        }
    }

    suspend fun getNearestPoint(keyName: Preferences.Key<Int>): Int {
        return appContext.dataStore.data.map { preferences ->
            preferences[keyName] ?: 0
        }.first()
    }
}

