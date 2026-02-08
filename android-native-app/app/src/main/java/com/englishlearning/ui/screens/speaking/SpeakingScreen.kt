package com.englishlearning.ui.screens.speaking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishlearning.ui.components.LoadingAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpeakingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startRecording(context)
        } else {
            // Show permission denied message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("口语练习") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SpeakingUiState.Loading -> {
                    LoadingAnimation(modifier = Modifier.align(Alignment.Center))
                }
                is SpeakingUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SpeakingUiState.Ready -> {
                    SpeakingContent(
                        targetText = state.targetText,
                        isRecording = false,
                        onRecordClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startRecording(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                }
                is SpeakingUiState.Recording -> {
                    SpeakingContent(
                        targetText = state.targetText,
                        isRecording = true,
                        onRecordClick = {
                             viewModel.stopRecording()
                        }
                    )
                }
                is SpeakingUiState.Result -> {
                    ResultContent(
                        targetText = state.targetText,
                        spokenText = state.spokenText,
                        score = state.score,
                        onRetry = { viewModel.retry() },
                        onNext = { viewModel.nextSegment() }
                    )
                }
            }
        }
    }
}

@Composable
fun SpeakingContent(
    targetText: String,
    isRecording: Boolean,
    onRecordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Target Sentence
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recording Indicator / Visualization
        if (isRecording) {
            Text(
                text = "正在录音...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            LoadingAnimation(
                circleColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Text(
                text = "点击麦克风开始跟读",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(50.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Record Button
        Button(
            onClick = onRecordClick,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop" else "Record",
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ResultContent(
    targetText: String,
    spokenText: String,
    score: Float,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Display
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    color = getScoreColor(score).copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${score.toInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    color = getScoreColor(score),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "分",
                    style = MaterialTheme.typography.titleMedium,
                    color = getScoreColor(score)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Comparison
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("原句:", style = MaterialTheme.typography.labelMedium)
                Text(targetText, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("你的发音:", style = MaterialTheme.typography.labelMedium)
                Text(
                    spokenText.ifEmpty { "(未识别到语音)" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (score > 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("下一句")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.SkipNext, null)
            }
        }
    }
}

fun getScoreColor(score: Float): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}
