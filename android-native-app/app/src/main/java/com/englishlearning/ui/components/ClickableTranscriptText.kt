package com.englishlearning.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * 可点击的文本段落 - 每个单词都可点击
 *
 * 使用示例：
 * ```
 * var selectedWord by remember { mutableStateOf<String?>(null) }
 *
 * ClickableTranscriptText(
 *     text = "Excuse me, is this your handbag?",
 *     onWordClick = { word -> selectedWord = word }
 * )
 *
 * // 显示单词弹窗
 * selectedWord?.let { word ->
 *     WordInfoDialog(
 *         word = word,
 *         onDismiss = { selectedWord = null }
 *     )
 * }
 * ```
 */
@Composable
fun ClickableTranscriptText(
    text: String,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    val words = text.split(Regex("(?=[a-zA-Z\\-'])|(?<=[a-zA-Z\\-'])"))

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0

        words.forEach { chunk ->
            if (chunk.isBlank()) {
                // 保留空格和标点
                append(chunk)
            } else if (chunk.matches(Regex("[a-zA-Z\\-']+"))) {
                // 这是单词
                if (chunk.length > 2) {
                    // 可点击的单词
                    withStyle(
                        style = SpanStyle(
                            color = textColor,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    ) {
                        append(chunk)
                    }
                    // 添加注解，用于点击检测
                    addStringAnnotation(
                        tag = "word",
                        annotation = chunk,
                        start = currentIndex,
                        end = currentIndex + chunk.length
                    )
                } else {
                    // 短词（<= 2个字符）不处理
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append(chunk)
                    }
                }
            } else {
                // 标点符号
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(chunk)
                }
            }
            currentIndex += chunk.length
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        onClick = { offset ->
            // 获取点击位置的单词
            annotatedString.getStringAnnotations(
                tag = "word",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                // 清理单词
                val cleanWord = annotation.item.replace(Regex("[^a-zA-Z\\-]"), "")
                if (cleanWord.length > 2) {
                    onWordClick(cleanWord.lowercase())
                }
            }
        }
    )
}

/**
 * 简化版本：单词间有空格的文本
 */
@Composable
fun SimpleClickableText(
    text: String,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val words = text.split(" ")

    val annotatedString = buildAnnotatedString {
        words.forEachIndexed { index, word ->
            val cleanWord = word.replace(Regex("[^a-zA-Z\\-']"), "")

            if (cleanWord.isNotEmpty() && cleanWord.length > 2) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                ) {
                    append(word)
                }

                addStringAnnotation(
                    tag = "word",
                    annotation = cleanWord,
                    start = length - word.length,
                    end = length
                )
            } else {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(word)
                }
            }

            // 添加空格（除了最后一个词）
            if (index < words.size - 1) {
                append(" ")
            }
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "word",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                onWordClick(annotation.item.lowercase())
            }
        }
    )
}
