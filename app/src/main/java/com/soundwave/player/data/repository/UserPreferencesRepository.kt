package com.soundwave.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.soundwave.player.ui.screens.settings.ThemeMode
import com.soundwave.player.ui.screens.settings.MiniPlayerStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val FILTER_SHORT_SONGS = booleanPreferencesKey("filter_short_songs")
        val MIN_SONG_DURATION = intPreferencesKey("min_song_duration")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val MINI_PLAYER_STYLE = stringPreferencesKey("mini_player_style")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(name)
    }

    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
    }

    val filterShortSongs: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FILTER_SHORT_SONGS] ?: true
    }

    val minSongDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MIN_SONG_DURATION] ?: 30
    }

    val excludedFolders: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: emptySet()
    }

    val miniPlayerStyle: Flow<MiniPlayerStyle> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.MINI_PLAYER_STYLE] ?: MiniPlayerStyle.DOCKED.name
        MiniPlayerStyle.valueOf(name)
    }

    val accentColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCENT_COLOR]
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setFilterShortSongs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_SHORT_SONGS] = enabled
        }
    }

    suspend fun setMinSongDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_SONG_DURATION] = duration
        }
    }

    suspend fun toggleExcludedFolder(folderPath: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: emptySet()
            val newSet = if (current.contains(folderPath)) {
                current - folderPath
            } else {
                current + folderPath
            }
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = newSet
        }
    }

    suspend fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MINI_PLAYER_STYLE] = style.name
        }
    }

    suspend fun setAccentColor(colorArgb: Int?) {
        context.dataStore.edit { preferences ->
            if (colorArgb != null) {
                preferences[PreferencesKeys.ACCENT_COLOR] = colorArgb
            } else {
                preferences.remove(PreferencesKeys.ACCENT_COLOR)
            }
        }
    }
}
