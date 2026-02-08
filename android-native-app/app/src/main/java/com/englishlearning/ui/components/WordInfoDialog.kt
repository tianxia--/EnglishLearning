package com.englishlearning.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.speech.tts.TextToSpeech
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import com.englishlearning.data.model.WordTranslation
import com.englishlearning.data.translation.TranslationService
import dagger.hilt.android.EntryPointAccessors
import java.util.*

/**
 * 单词信息弹窗 - 使用真实翻译API
 */
@Composable
fun WordInfoDialog(
    word: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 使用Hilt获取TranslationService
    val translationService = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TranslationServiceEntryPoint::class.java
        ).getTranslationService()
    }

    var wordTranslation by remember { mutableStateOf<WordTranslation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 使用LaunchedEffect加载数据
    LaunchedEffect(word) {
        isLoading = true
        errorMessage = null

        val result = translationService.translateWord(word)
        result.onSuccess { translation ->
            wordTranslation = translation
            isLoading = false
            android.util.Log.d("WordInfoDialog", "Translation loaded: ${translation.word}")
        }.onFailure { error ->
            errorMessage = error.message
            isLoading = false
            android.util.Log.e("WordInfoDialog", "Translation failed", error)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "单词详情",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 加载状态
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "正在查询...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (errorMessage != null) {
                    // 错误状态
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "查询失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "单词: $word",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 显示单词信息
                    wordTranslation?.let { translation ->
                        // 单词和发音
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // 单词本身
                                Text(
                                    text = translation.word,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // 音标
                                translation.phonetic?.let { phonetic ->
                                    Text(
                                        text = phonetic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                // 词性
                                translation.partOfSpeech?.let { pos ->
                                    Surface(
                                        modifier = Modifier.padding(top = 4.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = pos,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            // 发音按钮
                            PronunciationButton(word = word)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 分隔线
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 英文释义
                        Text(
                            text = "释义",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        translation.definition?.let { definition ->
                            Text(
                                text = definition,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 例句
                        translation.example?.let { example ->
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "例句",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = example,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 同义词
                        if (!translation.synonyms.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "同义词",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = translation.synonyms!!.take(5).joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 关闭按钮
                SunnyPrimaryButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    text = "知道了"
                )
            }
        }
    }
}

/**
 * Hilt入口点接口
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface TranslationServiceEntryPoint {
    fun getTranslationService(): TranslationService
}

/**
 * 发音按钮（带播放动画）
 */
@Composable
fun PronunciationButton(word: String) {
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val context = LocalContext.current
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    // 播放动画 - 脉冲缩放效果
    val infiniteTransition = rememberInfiniteTransition(label = "pronunciation_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 如果美式英语不支持，尝试英式英语
                    tts?.setLanguage(Locale.UK)
                }
                tts?.setSpeechRate(0.8f) // 稍慢的语速，更清晰
                isTtsReady = true
                android.util.Log.d("TTS", "TTS initialized successfully for word: $word")
            } else {
                android.util.Log.e("TTS", "TTS initialization failed, status: $status")
                isTtsReady = false
            }
        }
        onDispose {
            isTtsReady = false
            isSpeaking = false
            tts?.shutdown()
            tts = null
        }
    }

    // 根据播放状态应用动画
    val buttonModifier = if (isSpeaking) {
        Modifier.scale(scale)
    } else {
        Modifier
    }

    // 当开始播放时，自动停止动画（估算播放时长）
    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            // 根据单词长度估算播放时间（每个单词约200ms）
            val estimatedDuration = (word.length * 200L)
            delay(estimatedDuration)
            isSpeaking = false
        }
    }

    SunnySecondaryButton(
        onClick = {
            android.util.Log.d("TTS", "Pronunciation button clicked, word: $word, TTS ready: $isTtsReady")
            if (isTtsReady && tts != null && !isSpeaking) {
                isSpeaking = true
                val speakResult = tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_pronunciation")
                android.util.Log.d("TTS", "Speak result: $speakResult")
            } else if (isSpeaking) {
                android.util.Log.d("TTS", "Already speaking, ignoring click")
            } else {
                android.util.Log.e("TTS", "TTS not ready, isReady: $isTtsReady, tts: $tts")
            }
        },
        modifier = buttonModifier,
        icon = Icons.Default.VolumeUp,
        text = if (isSpeaking) "播放中..." else if (isTtsReady) "发音" else "加载中",
        enabled = isTtsReady && !isSpeaking
    )
}
