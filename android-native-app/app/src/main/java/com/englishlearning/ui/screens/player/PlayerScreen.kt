package com.englishlearning.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishlearning.service.AudioServiceManager
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Audio Player Screen with synchronized transcript
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSpeaking: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val currentSegmentIndex by viewModel.currentSegmentIndex.collectAsStateWithLifecycle()
    val durationMs by viewModel.duration.collectAsStateWithLifecycle()
    val isTranscriptVisible by viewModel.isTranscriptVisible.collectAsStateWithLifecycle()
    val autoPlayNext by viewModel.autoPlayNext.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val serviceManager = remember { AudioServiceManager(context) }

    // Start background service when screen is displayed
    LaunchedEffect(Unit) {
        android.util.Log.d("PlayerScreen", "PlayerScreen initialized, autoPlayNext: $autoPlayNext")
        serviceManager.startService()
    }

    // Stop service when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Don't stop service here - let it run in background
            // serviceManager.stopService()
        }
    }

    // Auto-scroll to current segment
    LaunchedEffect(currentSegmentIndex) {
        if (currentSegmentIndex >= 0) {
            lazyListState.animateScrollToItem(currentSegmentIndex)
        }
    }
    
    val aiExplanationState by viewModel.aiExplanationState.collectAsStateWithLifecycle()
    
    // AI Explanation Dialog
    when (val state = aiExplanationState) {
        is PlayerViewModel.AiExplanationState.Success -> {
            com.englishlearning.ui.components.AIExplanationDialog(
                explanation = state.explanation,
                onDismiss = { viewModel.clearExplanation() }
            )
        }
        is PlayerViewModel.AiExplanationState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearExplanation() },
                title = { Text("Error") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearExplanation() }) {
                        Text("OK")
                    }
                }
            )
        }
        is PlayerViewModel.AiExplanationState.Loading -> {
             AlertDialog(
                onDismissRequest = {},
                text = {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         CircularProgressIndicator(modifier = Modifier.size(24.dp))
                         Spacer(modifier = Modifier.width(16.dp))
                         Text("Asking AI tutor...")
                     }
                },
                confirmButton = {}
             )
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Player") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Speaking Practice
                    IconButton(onClick = { 
                        if (viewModel.uiState.value is PlayerViewModel.PlayerUiState.Success) {
                            val lesson = (viewModel.uiState.value as PlayerViewModel.PlayerUiState.Success).lesson
                            onNavigateToSpeaking(lesson.bookId, lesson.id)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Practice Speaking"
                        )
                    }

                    // Auto-play next toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.toggleAutoPlayNext() }
                    ) {
                        Icon(
                            imageVector = if (autoPlayNext) Icons.Default.CheckCircle
                            else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (autoPlayNext) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (autoPlayNext) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Transcript visibility toggle
                    IconButton(onClick = { viewModel.toggleTranscriptVisibility() }) {
                        Icon(
                            if (isTranscriptVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Transcript"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is PlayerViewModel.PlayerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PlayerViewModel.PlayerUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as PlayerViewModel.PlayerUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            is PlayerViewModel.PlayerUiState.Success -> {
                val lesson = (uiState as PlayerViewModel.PlayerUiState.Success).lesson

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Lesson info
                    LessonInfoHeader(
                        title = lesson.title,
                        lessonNumber = lesson.lessonNumber,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Transcript or placeholder
                    if (isTranscriptVisible && lesson.segments.isNotEmpty()) {
                        TranscriptView(
                            segments = lesson.segments,
                            currentSegmentIndex = currentSegmentIndex,
                            onSegmentClick = { index -> viewModel.onSegmentClick(index) },
                            onSentenceLongClick = { sentence -> viewModel.explainSentence(sentence) },
                            lazyListState = lazyListState,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Audio playing...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Player controls
                    PlayerControls(
                        playbackState = playbackState,
                        currentPosition = currentPosition,
                        duration = if (durationMs > 0L) {
                            durationMs
                        } else {
                            // Fallback to lesson metadata (seconds -> ms) if player duration unavailable
                            (lesson.duration * 1000).toLong()
                        },
                        playbackSpeed = playbackSpeed,
                        onPlayPause = {
                            when (playbackState) {
                                is PlaybackState.Playing -> viewModel.pause()
                                else -> viewModel.play()
                            }
                        },
                        onSkipForward = { viewModel.skipForward() },
                        onSkipBackward = { viewModel.skipBackward() },
                        onSeek = { position -> viewModel.seekTo(position) },
                        onSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) },
                        onStop = { viewModel.stop() }
                    )
                }
            }
        }
    }
}

@Composable
fun LessonInfoHeader(
    title: String,
    lessonNumber: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Lesson $lessonNumber",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TranscriptView(
    segments: List<com.englishlearning.data.model.Segment>,
    currentSegmentIndex: Int,
    onSegmentClick: (Int) -> Unit,
    onSentenceLongClick: (String) -> Unit,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    var selectedWord by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(segments) { index, segment ->
            TranscriptSegment(
                text = segment.text,
                isHighlighted = index == currentSegmentIndex,
                onClick = { onSegmentClick(index) },
                onLongClick = { onSentenceLongClick(segment.text) },
                onWordClick = { word -> selectedWord = word }
            )
        }
    }

    // 单词详情弹窗
    selectedWord?.let { word ->
        com.englishlearning.ui.components.WordInfoDialog(
            word = word,
            onDismiss = { selectedWord = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranscriptSegment(
    text: String,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0f,
        label = "backgroundAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 将文本分割成可点击的单词
            ClickableTranscriptText(
                text = text,
                isHighlighted = isHighlighted,
                onWordClick = onWordClick
            )
        }
    }
}

/**
 * 可点击的转录文本 - 单词可点击查词（支持自动换行）
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ClickableTranscriptText(
    text: String,
    isHighlighted: Boolean,
    onWordClick: (String) -> Unit
) {
    val words = text.split(Regex("\\s+"))

    // 使用FlowRow支持自动换行
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEach { word ->
            val cleanWord = word.replace(Regex("[^a-zA-Z\\-']"), "")

            if (cleanWord.isNotEmpty() && cleanWord.length > 2) {
                // 可点击的单词
                androidx.compose.foundation.text.ClickableText(
                    text = androidx.compose.ui.text.buildAnnotatedString {
                        withStyle(
                            style = androidx.compose.ui.text.SpanStyle(
                                color = if (isHighlighted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        ) {
                            append(word)
                        }
                    },
                    onClick = { onWordClick(cleanWord) },
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // 普通文本（标点符号、短词）
                Text(
                    text = word,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHighlighted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                )
            }
            // 添加空格
            Text(
                text = " ",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PlayerControls(
    playbackState: PlaybackState,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onStop: () -> Unit
) {
    var isSpeedMenuExpanded by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(currentPosition) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Progress bar
        Slider(
            value = if (isDragging) dragPosition.toFloat() else currentPosition.toFloat(),
            onValueChange = {
                isDragging = true
                dragPosition = it.toLong()
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(dragPosition)
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed control
            Box {
                TextButton(onClick = { isSpeedMenuExpanded = true }) {
                    Text("${playbackSpeed}x")
                }
                DropdownMenu(
                    expanded = isSpeedMenuExpanded,
                    onDismissRequest = { isSpeedMenuExpanded = false }
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x") },
                            onClick = {
                                onSpeedChange(speed)
                                isSpeedMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Skip backward
            IconButton(onClick = onSkipBackward) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Skip Backward",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause button
            FloatingActionButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (playbackState is PlaybackState.Playing) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (playbackState is PlaybackState.Playing) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Skip forward
            IconButton(onClick = onSkipForward) {
                Icon(
                    imageVector = Icons.Default.Forward30,
                    contentDescription = "Skip Forward",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Stop button
            IconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Format time in milliseconds to MM:SS format
 */
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
