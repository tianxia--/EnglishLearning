package com.englishlearning.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishlearning.data.model.Book
import com.englishlearning.domain.DailyTask
import com.englishlearning.ui.components.BookCard
import com.englishlearning.ui.components.LoadingScreen
import com.englishlearning.ui.screens.home.HomeViewModel.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPlayer: (String, String) -> Unit = { _, _ -> },
    onNavigateToTranscription: (String, String) -> Unit = { _, _ -> },
    onNavigateToQuiz: (String, String) -> Unit = { _, _ -> },
    onNavigateToFlashcards: () -> Unit = {},
    onNavigateToSpeaking: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    LoadingScreen()
                }
                is HomeUiState.Success -> {
                    val books = (uiState as HomeUiState.Success).books
                    
                    if (books.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No content found.\nPlease check assets/indexed_lessons.json",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        DashboardContent(
                            dailyTasks = dailyTasks,
                            books = books,
                            onTaskClick = { task ->
                                 when (task) {
                                     is DailyTask.Learn -> onNavigateToPlayer(task.book.id, task.lesson.id)
                                     is DailyTask.ReviewVocabulary -> onNavigateToFlashcards()
                                     is DailyTask.SpeakingChallenge -> {
                                         // Find a random completed lesson or just use the learn task's lesson
                                         // For simplicity/demo, use book 1 lesson 1 if available or the learn task's lesson
                                         if (task is DailyTask.Learn) { 
                                             // If it's a learn task, use that lesson
                                             onNavigateToSpeaking(task.book.id, task.lesson.id)
                                         } else {
                                             // Fallback to first lesson of first book
                                              if (books.isNotEmpty()) {
                                                  // Construct ID assuming valid format: bookId_lesson_001
                                                  // Or if we can't know, maybe hardcode book1_lesson_001 for now as safe default
                                                  val bookId = books[0].id
                                                  val lessonId = "${bookId}_lesson_001"
                                                  onNavigateToSpeaking(bookId, lessonId) 
                                              }
                                         }
                                     }
                                 }
                            },
                            onBookClick = { book ->
                                 // Navigate to first lesson of the book
                                 onNavigateToPlayer(book.id, "${book.id}_lesson_001")
                            },
                            onNavigateToPlayer = onNavigateToPlayer
                        )
                    }
                }

                is HomeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: " + (uiState as HomeUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    dailyTasks: List<DailyTask>,
    books: List<Book>,
    onTaskClick: (DailyTask) -> Unit,
    onBookClick: (Book) -> Unit,
    onNavigateToPlayer: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Hero Section (Daily Focus)
        item {
            val mainTask = dailyTasks.filterIsInstance<DailyTask.Learn>().firstOrNull()
            if (mainTask != null) {
                DailyFocusCard(task = mainTask, onClick = { onTaskClick(mainTask) })
            }
        }

        // 2. Today's Tasks
        item {
            Text(
                text = "For You",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vocabulary
                item {
                    val vocabTask = dailyTasks.filterIsInstance<DailyTask.ReviewVocabulary>().firstOrNull()
                    if (vocabTask != null) {
                        TaskCard(
                            title = "Review Words",
                            subtitle = "${vocabTask.count} due today",
                            icon = Icons.Default.Style,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = { onTaskClick(vocabTask) }
                        )
                    } else {
                        // Always show vocab option even if not generated task? 
                        TaskCard(
                            title = "Vocabulary",
                            subtitle = "Practice words",
                            icon = Icons.Default.Style,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { onTaskClick(DailyTask.ReviewVocabulary(0)) }
                        )
                    }
                }
                
                // Speaking
                item {
                    TaskCard(
                        title = "Speaking",
                        subtitle = "Daily Challenge",
                        icon = Icons.Default.Mic,
                        color = MaterialTheme.colorScheme.errorContainer, // Orange-ish usually
                        onClick = { onTaskClick(DailyTask.SpeakingChallenge) }
                    )
                }
            }
        }

        // 3. Library
        item {
            Text(
                text = "Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(books) { book ->
            // Use existing BookCard but maybe handle click to open a dialog or navigate
            // Since we removed the "Selected Book" state from Home to simplify Dashboard, 
            // we can just let clicking a book go to its first lesson for now, 
            // or we can re-introduce a "BookDetailScreen" later.
            // For MVP Refactor, let's keep it simple.
            BookCard(
                book = book,
                onClick = { 
                     // Temporary: just go to lesson 1
                     onNavigateToPlayer(book.id, "${book.id}-01")
                }
            )
        }
    }
}

@Composable
fun DailyFocusCard(
    task: DailyTask.Learn,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    text = "DAILY FOCUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.lesson.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PlayArrow, "Start")
            }
        }
    }
}

@Composable
fun TaskCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 140.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
