package com.englishlearning.ui.screens.transcription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishlearning.data.model.Segment
import com.englishlearning.ui.screens.transcription.TranscriptionViewModel.TranscriptionUiState

/**
 * Transcription Exercise Screen - Listen and type what you hear
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionScreen(
    viewModel: TranscriptionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSegmentIndex by viewModel.currentSegmentIndex.collectAsStateWithLifecycle()
    val userInput by viewModel.userInput.collectAsStateWithLifecycle()
    val showHint by viewModel.showHint.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transcription Exercise") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text("Score: ${score}%")
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is TranscriptionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TranscriptionUiState.Ready -> {
                val lesson = (uiState as TranscriptionUiState.Ready).lesson
                val segment = lesson.segments.getOrNull(currentSegmentIndex)

                ExerciseContent(
                    segment = segment,
                    segmentIndex = currentSegmentIndex,
                    totalSegments = lesson.segments.size,
                    userInput = userInput,
                    showHint = showHint,
                    hint = viewModel.getHint(),
                    onUserInputChange = { viewModel.onUserInputChange(it) },
                    onPlay = { viewModel.playSegment(currentSegmentIndex) },
                    onSubmit = { viewModel.submitAnswer() },
                    onShowHint = { viewModel.showHint() },
                    onReveal = { viewModel.revealAnswer() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is TranscriptionUiState.Correct -> {
                val state = uiState as TranscriptionUiState.Correct
                FeedbackScreen(
                    isCorrect = true,
                    userText = state.userText,
                    correctText = state.correctText,
                    segmentIndex = state.segmentIndex,
                    totalSegments = state.lesson.segments.size,
                    score = score,
                    onNext = { viewModel.nextSegment() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is TranscriptionUiState.Incorrect -> {
                val state = uiState as TranscriptionUiState.Incorrect
                FeedbackScreen(
                    isCorrect = false,
                    userText = state.userText,
                    correctText = state.correctText,
                    differences = state.differences,
                    segmentIndex = state.segmentIndex,
                    totalSegments = state.lesson.segments.size,
                    score = score,
                    onNext = { viewModel.nextSegment() },
                    onRetry = { viewModel.retrySegment() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is TranscriptionUiState.Revealed -> {
                val state = uiState as TranscriptionUiState.Revealed
                AnswerRevealedScreen(
                    correctText = state.correctText,
                    segmentIndex = state.segmentIndex,
                    totalSegments = state.lesson.segments.size,
                    onNext = { viewModel.nextSegment() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is TranscriptionUiState.Completed -> {
                val state = uiState as TranscriptionUiState.Completed
                CompletionScreen(
                    score = state.score,
                    correctAnswers = state.correctAnswers,
                    totalSegments = state.totalSegments,
                    onReset = { viewModel.resetExercise() },
                    onBack = onNavigateBack,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is TranscriptionUiState.Error -> {
                ErrorScreen(
                    message = (uiState as TranscriptionUiState.Error).message,
                    onBack = onNavigateBack,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ExerciseContent(
    segment: com.englishlearning.data.model.Segment?,
    segmentIndex: Int,
    totalSegments: Int,
    userInput: String,
    showHint: Boolean,
    hint: String,
    onUserInputChange: (String) -> Unit,
    onPlay: () -> Unit,
    onSubmit: () -> Unit,
    onShowHint: () -> Unit,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { (segmentIndex + 1).toFloat() / totalSegments.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Segment ${segmentIndex + 1} of $totalSegments",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Play button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Listen to the audio",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Type what you hear",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Audio")
                }
            }
        }

        // Hint (if shown)
        if (showHint) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Hint",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Text input
        OutlinedTextField(
            value = userInput,
            onValueChange = onUserInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Type what you heard") },
            placeholder = { Text("Start typing...") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            minLines = 3,
            maxLines = 5
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowHint,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hint")
            }

            OutlinedButton(
                onClick = onReveal,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reveal")
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Submit")
            }
        }
    }
}

@Composable
fun FeedbackScreen(
    isCorrect: Boolean,
    userText: String,
    correctText: String,
    differences: List<com.englishlearning.ui.screens.transcription.TranscriptionViewModel.Diff>? = null,
    segmentIndex: Int,
    totalSegments: Int,
    score: Int,
    onNext: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Icon
        Icon(
            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (isCorrect) {
                Color(0xFF4CAF50)
            } else {
                Color(0xFFF44336)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = if (isCorrect) "Correct! 🎉" else "Not Quite",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Text comparison
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your answer:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userText,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!isCorrect && differences != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DifferencesList(differences)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Correct answer:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = correctText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onRetry != null) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retry")
                }
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun DifferencesList(
    differences: List<com.englishlearning.ui.screens.transcription.TranscriptionViewModel.Diff>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        differences.forEach { diff ->
            when (diff.type) {
                com.englishlearning.ui.screens.transcription.TranscriptionViewModel.DiffType.WRONG -> {
                    Row {
                        Text(
                            text = "❌ ",
                            color = Color(0xFFF44336)
                        )
                        Text(
                            text = "\"${diff.userWord}\" → ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            text = "\"${diff.correctWord}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                com.englishlearning.ui.screens.transcription.TranscriptionViewModel.DiffType.MISSING -> {
                    Row {
                        Text(
                            text = "➖ ",
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "Missing: \"${diff.correctWord}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                com.englishlearning.ui.screens.transcription.TranscriptionViewModel.DiffType.EXTRA -> {
                    Row {
                        Text(
                            text = "➕ ",
                            color = Color(0xFF2196F3)
                        )
                        Text(
                            text = "Extra: \"${diff.userWord}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerRevealedScreen(
    correctText: String,
    segmentIndex: Int,
    totalSegments: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Answer Revealed",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Correct answer:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = correctText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Next Segment")
        }
    }
}

@Composable
fun CompletionScreen(
    score: Int,
    correctAnswers: Int,
    totalSegments: Int,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Exercise Complete! 🎉",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Final Score
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$correctAnswers of $totalSegments correct",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Lessons")
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Go Back")
        }
    }
}
