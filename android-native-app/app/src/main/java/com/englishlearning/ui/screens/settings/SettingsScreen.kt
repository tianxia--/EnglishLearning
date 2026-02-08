package com.englishlearning.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.englishlearning.data.preferences.FontSize
import com.englishlearning.ui.screens.settings.AIConfigDialog
import kotlinx.coroutines.launch

/**
 * Settings Screen - App settings and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToSignature: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // 对话框状态
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showAIConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        SettingsContent(
            settings = settings,
            onSettingsChange = { key, value -> viewModel.updateSetting(key, value) },
            onSpeedClick = { showSpeedDialog = true },
            onGoalClick = { showGoalDialog = true },
            onFontSizeClick = { showFontSizeDialog = true },
            onClearCacheClick = { showClearCacheDialog = true },
            onAIConfigClick = { showAIConfigDialog = true },
            onNavigateToSignature = onNavigateToSignature,
            modifier = Modifier.padding(paddingValues)
        )
    }

    // 播放速度对话框
    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = settings.defaultPlaybackSpeed,
            onDismiss = { showSpeedDialog = false },
            onSpeedSelected = { speed ->
                viewModel.updateSetting("defaultPlaybackSpeed", speed)
            }
        )
    }

    // 每日学习目标对话框
    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = settings.dailyGoalMinutes,
            onDismiss = { showGoalDialog = false },
            onGoalSelected = { minutes ->
                viewModel.updateSetting("dailyGoalMinutes", minutes)
            }
        )
    }

    // 字体大小对话框
    if (showFontSizeDialog) {
        FontSizeDialog(
            currentSize = settings.fontSize,
            onDismiss = { showFontSizeDialog = false },
            onSizeSelected = { size ->
                viewModel.updateSetting("fontSize", size)
            }
        )
    }

    // AI Configuration Dialog
    if (showAIConfigDialog) {
        AIConfigDialog(
            currentApiKey = settings.aiApiKey,
            currentBaseUrl = settings.aiBaseUrl,
            currentModel = settings.aiModel,
            onDismiss = { showAIConfigDialog = false },
            onConfirm = { apiKey, baseUrl, model ->
                viewModel.updateAIConfig(apiKey, baseUrl, model)
                showAIConfigDialog = false
            }
        )
    }

    // 清除缓存确认对话框
    if (showClearCacheDialog) {
        ConfirmDialog(
            title = "清除缓存",
            message = "确定要清除所有缓存数据吗？这不会删除您的学习进度。",
            onDismiss = { showClearCacheDialog = false },
            onConfirm = {
                isClearingCache = true
                // 清除缓存
                scope.launch {
                    val success = viewModel.clearCache()
                    isClearingCache = false
                    if (success) {
                        android.util.Log.d("SettingsScreen", "Cache cleared successfully")
                    } else {
                        android.util.Log.e("SettingsScreen", "Failed to clear cache")
                    }
                }
            }
        )
    }

    // 清除缓存加载对话框
    if (isClearingCache) {
        LoadingDialog(
            onDismiss = {},
            message = "正在清除缓存..."
        )
    }
}

@Composable
fun SettingsContent(
    settings: AppSettings,
    onSettingsChange: (String, Any) -> Unit,
    onSpeedClick: () -> Unit,
    onGoalClick: () -> Unit,
    onFontSizeClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onAIConfigClick: () -> Unit,
    onNavigateToSignature: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Playback Settings Section
        item {
            SettingsSectionHeader("播放设置")
        }

        item {
            SettingsItem(
                icon = Icons.Default.Speed,
                title = "默认播放速度",
                subtitle = "${settings.defaultPlaybackSpeed}x",
                onClick = onSpeedClick
            )
        }

        item {
            SwitchSettingsItem(
                icon = Icons.Default.PlayArrow,
                title = "自动播放下一课",
                checked = settings.autoPlayNext,
                onCheckedChange = { onSettingsChange("autoPlayNext", it) }
            )
        }

        item {
            SwitchSettingsItem(
                icon = Icons.Default.Visibility,
                title = "默认显示字幕",
                checked = settings.showTranscriptByDefault,
                onCheckedChange = { onSettingsChange("showTranscriptByDefault", it) }
            )
        }

        // Study Settings Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("学习设置")
        }

        item {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "学习提醒",
                subtitle = if (settings.studyReminderEnabled) "已开启" else "已关闭",
                onClick = { /* Show reminder settings */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.School,
                title = "每日学习目标",
                subtitle = "${settings.dailyGoalMinutes} 分钟",
                onClick = onGoalClick
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.SmartToy,
                title = "AI 助手配置",
                subtitle = if (settings.aiApiKey.isNotBlank()) "已配置" else "未配置",
                onClick = onAIConfigClick
            )
        }

        // App Settings Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("应用设置")
        }

        item {
            SwitchSettingsItem(
                icon = Icons.Default.DarkMode,
                title = "深色模式",
                checked = settings.darkMode,
                onCheckedChange = { onSettingsChange("darkMode", it) }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.FontDownload,
                title = "字体大小",
                subtitle = when (settings.fontSize) {
                    FontSize.SMALL -> "小"
                    FontSize.MEDIUM -> "中"
                    FontSize.LARGE -> "大"
                },
                onClick = onFontSizeClick
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Create,
                title = "手写签名",
                subtitle = "创建和管理您的签名",
                onClick = onNavigateToSignature
            )
        }

        // Data Management Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("数据管理")
        }

        item {
            SettingsItem(
                icon = Icons.Default.CloudUpload,
                title = "数据备份",
                subtitle = "备份学习进度",
                onClick = { /* Show backup dialog */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = "数据恢复",
                subtitle = "从备份恢复",
                onClick = { /* Show restore dialog */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "清除缓存",
                subtitle = "清除应用缓存数据",
                onClick = onClearCacheClick
            )
        }

        // About Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionHeader("关于")
        }

        item {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "应用版本",
                subtitle = "1.0.0",
                onClick = { }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Help,
                title = "帮助与反馈",
                subtitle = "使用帮助和问题反馈",
                onClick = { /* Show help */ }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
