package com.englishlearning.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore扩展
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好设置管理器 - 统一管理应用设置
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 定义keys
    private object PreferencesKeys {
        val DEFAULT_PLAYBACK_SPEED = floatPreferencesKey("default_playback_speed")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val SHOW_TRANSCRIPT_BY_DEFAULT = booleanPreferencesKey("show_transcript_by_default")
        val STUDY_REMINDER_ENABLED = booleanPreferencesKey("study_reminder_enabled")
        val DAILY_GOAL_MINUTES = stringPreferencesKey("daily_goal_minutes")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        
        // AI Configuration
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val AI_MODEL = stringPreferencesKey("ai_model")

        // Playback history keys
        val LAST_PLAYED_BOOK_ID = stringPreferencesKey("last_played_book_id")
        val LAST_PLAYED_LESSON_ID = stringPreferencesKey("last_played_lesson_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        val LAST_PLAYED_TIMESTAMP = longPreferencesKey("last_played_timestamp")
    }

    // 获取所有设置的Flow
    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            defaultPlaybackSpeed = preferences[PreferencesKeys.DEFAULT_PLAYBACK_SPEED] ?: 1.0f,
            autoPlayNext = preferences[PreferencesKeys.AUTO_PLAY_NEXT] ?: false,
            showTranscriptByDefault = preferences[PreferencesKeys.SHOW_TRANSCRIPT_BY_DEFAULT] ?: true,
            studyReminderEnabled = preferences[PreferencesKeys.STUDY_REMINDER_ENABLED] ?: false,
            dailyGoalMinutes = preferences[PreferencesKeys.DAILY_GOAL_MINUTES]?.toIntOrNull() ?: 30,
            darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
            fontSize = FontSize.valueOf(
                preferences[PreferencesKeys.FONT_SIZE] ?: FontSize.MEDIUM.name
            ),
            aiApiKey = preferences[PreferencesKeys.AI_API_KEY] ?: "",
            aiBaseUrl = preferences[PreferencesKeys.AI_BASE_URL] ?: "https://api.openai.com/v1/",
            aiModel = preferences[PreferencesKeys.AI_MODEL] ?: "gpt-3.5-turbo"
        )
    }

    // 获取播放历史
    val playbackHistory: Flow<PlaybackHistory?> = context.dataStore.data.map { preferences ->
        val bookId = preferences[PreferencesKeys.LAST_PLAYED_BOOK_ID]
        val lessonId = preferences[PreferencesKeys.LAST_PLAYED_LESSON_ID]
        val position = preferences[PreferencesKeys.LAST_PLAYED_POSITION] ?: 0L
        val timestamp = preferences[PreferencesKeys.LAST_PLAYED_TIMESTAMP] ?: 0L

        if (bookId != null && lessonId != null) {
            PlaybackHistory(
                bookId = bookId,
                lessonId = lessonId,
                position = position,
                timestamp = timestamp
            )
        } else {
            null
        }
    }

    // 更新播放速度
    suspend fun updateDefaultPlaybackSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PLAYBACK_SPEED] = speed
        }
    }

    // 更新自动播放下一课
    suspend fun updateAutoPlayNext(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_PLAY_NEXT] = enabled
        }
    }

    // 更新默认显示字幕
    suspend fun updateShowTranscriptByDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TRANSCRIPT_BY_DEFAULT] = enabled
        }
    }

    // 更新学习提醒
    suspend fun updateStudyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STUDY_REMINDER_ENABLED] = enabled
        }
    }

    // 更新每日学习目标
    suspend fun updateDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL_MINUTES] = minutes.toString()
        }
    }

    // 更新深色模式
    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    // 更新字体大小
    suspend fun updateFontSize(fontSize: FontSize) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize.name
        }
    }
    
    // 更新 AI 设置
    suspend fun updateAIConfiguration(apiKey: String, baseUrl: String, model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_API_KEY] = apiKey
            preferences[PreferencesKeys.AI_BASE_URL] = baseUrl
            preferences[PreferencesKeys.AI_MODEL] = model
        }
    }

    // 保存播放历史
    suspend fun savePlaybackHistory(bookId: String, lessonId: String, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_BOOK_ID] = bookId
            preferences[PreferencesKeys.LAST_PLAYED_LESSON_ID] = lessonId
            preferences[PreferencesKeys.LAST_PLAYED_POSITION] = position
            preferences[PreferencesKeys.LAST_PLAYED_TIMESTAMP] = System.currentTimeMillis()
        }
        android.util.Log.d("UserPreferencesManager", "Saved playback history: $bookId/$lessonId at ${position}ms")
    }

    // 清除播放历史
    suspend fun clearPlaybackHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_PLAYED_BOOK_ID)
            preferences.remove(PreferencesKeys.LAST_PLAYED_LESSON_ID)
            preferences.remove(PreferencesKeys.LAST_PLAYED_POSITION)
            preferences.remove(PreferencesKeys.LAST_PLAYED_TIMESTAMP)
        }
        android.util.Log.d("UserPreferencesManager", "Cleared playback history")
    }
}

/**
 * 用户偏好设置数据类
 */
data class UserPreferences(
    val defaultPlaybackSpeed: Float = 1.0f,
    val autoPlayNext: Boolean = false,
    val showTranscriptByDefault: Boolean = true,
    val studyReminderEnabled: Boolean = false,
    val dailyGoalMinutes: Int = 30,
    val darkMode: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val aiApiKey: String = "",
    val aiBaseUrl: String = "https://api.openai.com/v1/",
    val aiModel: String = "gpt-3.5-turbo"
)

/**
 * 字体大小枚举
 */
enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * 播放历史数据类
 */
data class PlaybackHistory(
    val bookId: String,
    val lessonId: String,
    val position: Long,
    val timestamp: Long
)

