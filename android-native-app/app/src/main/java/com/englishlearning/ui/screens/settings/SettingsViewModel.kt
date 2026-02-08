package com.englishlearning.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.cache.CacheManager
import com.englishlearning.data.preferences.FontSize
import com.englishlearning.data.preferences.UserPreferences
import com.englishlearning.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * ViewModel for settings screen - 使用统一的设置管理器
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val cacheManager: CacheManager
) : ViewModel() {

    // 从统一的设置管理器获取设置 - 使用stateIn转换为StateFlow
    val settings: StateFlow<UserPreferences> = userPreferencesManager.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    /**
     * 更新设置 - 使用DataStore持久化
     */
    fun updateSetting(key: String, value: Any) {
        viewModelScope.launch {
            when (key) {
                "autoPlayNext" -> {
                    userPreferencesManager.updateAutoPlayNext(value as Boolean)
                }
                "showTranscriptByDefault" -> {
                    userPreferencesManager.updateShowTranscriptByDefault(value as Boolean)
                }
                "studyReminderEnabled" -> {
                    userPreferencesManager.updateStudyReminderEnabled(value as Boolean)
                }
                "darkMode" -> {
                    userPreferencesManager.updateDarkMode(value as Boolean)
                }
                "fontSize" -> {
                    userPreferencesManager.updateFontSize(value as FontSize)
                }
                "defaultPlaybackSpeed" -> {
                    userPreferencesManager.updateDefaultPlaybackSpeed(value as Float)
                }
                "dailyGoalMinutes" -> {
                    userPreferencesManager.updateDailyGoalMinutes(value as Int)
                }
            }
            android.util.Log.d("SettingsViewModel", "Setting updated and saved: $key = $value")
        }
    }

    /**
     * 更新 AI 配置
     */
    fun updateAIConfig(apiKey: String, baseUrl: String, model: String) {
        viewModelScope.launch {
            userPreferencesManager.updateAIConfiguration(apiKey, baseUrl, model)
        }
    }

    /**
     * 获取缓存大小
     */
    fun getCacheSize(): String {
        return cacheManager.formatSize(
            // 同步获取缓存大小，实际应用中可以使用Flow
            runBlocking {
                cacheManager.getCacheSize()
            }
        )
    }

    /**
     * 清除缓存
     */
    suspend fun clearCache(): Boolean {
        return cacheManager.clearCache()
    }
}

// 为了兼容性，保留旧的别名
typealias AppSettings = UserPreferences