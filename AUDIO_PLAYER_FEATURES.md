# Audio Player Implementation Details

## Overview

The audio player is implemented with **ExoPlayer** (Media3) and features **real-time LRC synchronization** for displaying synchronized transcripts.

## Architecture

### Components

#### 1. AudioManager
- **Location**: `ui/screens/player/AudioManager.kt`
- **Purpose**: Wraps ExoPlayer and manages audio playback
- **Features**:
  - Play/Pause/Stop controls
  - Seek to position
  - Skip forward/backward (10 seconds)
  - Playback speed adjustment (0.5x - 2.0x)
  - State management (Idle, Buffering, Playing, Paused, Ended)

```kotlin
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Long)
    fun skipForward(milliseconds: Long = 10000L)
    fun skipBackward(milliseconds: Long = 10000L)
    fun setPlaybackSpeed(speed: Float)
}
```

#### 2. LrcSyncManager
- **Location**: `ui/screens/player/LrcSyncManager.kt`
- **Purpose**: Manages LRC transcript synchronization with audio
- **Features**:
  - Load segments from lesson data
  - Update current segment based on playback position
  - Jump to specific segment
  - Toggle sync on/off
  - Highlight current segment

```kotlin
class LrcSyncManager @Inject constructor() {
    fun loadSegments(segmentList: List<Segment>)
    fun updatePosition(positionSeconds: Double)
    fun getCurrentSegment(): Segment?
    fun jumpToSegment(index: Int): Double?
    fun toggleSync()
}
```

#### 3. PlayerViewModel
- **Location**: `ui/screens/player/PlayerViewModel.kt`
- **Purpose**: Coordinates AudioManager and LrcSyncManager
- **Features**:
  - Load lesson from ContentRepository
  - Manage playback state
  - Track study time
  - Save progress to database
  - Handle position tracking for sync

```kotlin
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val lessonRepository: LessonRepository,
    private val audioManager: AudioManager,
    private val lrcSyncManager: LrcSyncManager
) : ViewModel()
```

#### 4. PlayerScreen
- **Location**: `ui/screens/player/PlayerScreen.kt`
- **Purpose**: Jetpack Compose UI for audio player
- **Features**:
  - Lesson info header
  - Synchronized transcript view
  - Playback controls
  - Progress bar
  - Speed selector
  - Segment highlighting

## UI Components

### PlayerControls
```kotlin
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
    onSpeedChange: (Float) -> Unit
)
```

Features:
- **Slider**: Seek through audio
- **Time labels**: Current position and total duration
- **Play/Pause button**: Large FAB in center
- **Skip buttons**: Skip forward/backward 10 seconds
- **Speed selector**: Dropdown with 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x
- **Stop button**: Stop playback and reset

### TranscriptView
```kotlin
@Composable
fun TranscriptView(
    segments: List<Segment>,
    currentSegmentIndex: Int,
    onSegmentClick: (Int) -> Unit,
    lazyListState: LazyListState
)
```

Features:
- **LazyColumn**: Efficient rendering of long transcripts
- **Auto-scroll**: Automatically scrolls to current segment
- **Segment highlighting**: Current segment is highlighted
- **Click to jump**: Click any segment to jump to that position
- **Smooth animations**: Animated transitions between segments

### TranscriptSegment
```kotlin
@Composable
fun TranscriptSegment(
    text: String,
    isHighlighted: Boolean,
    onClick: () -> Unit
)
```

Features:
- **Card component**: Material 3 card design
- **Highlight**: Background color change for current segment
- **Bold text**: Current segment uses bold font
- **Click handler**: Click to jump to position

## Synchronization Logic

### How LRC Sync Works

1. **Load Segments**: When lesson loads, segments are loaded into `LrcSyncManager`
   ```kotlin
   lrcSyncManager.loadSegments(lesson.segments)
   ```

2. **Position Tracking**: Every 100ms, current playback position is checked
   ```kotlin
   LaunchedEffect(currentSegmentIndex) {
       while (true) {
           delay(100)
           val positionSeconds = audioManager.getCurrentPositionSeconds()
           lrcSyncManager.updatePosition(positionSeconds)
       }
   }
   ```

3. **Find Current Segment**: Manager finds the last segment that started before current position
   ```kotlin
   fun updatePosition(positionSeconds: Double) {
       val index = segments.indexOfLast { it.startTime <= positionSeconds }
       if (index >= 0 && index != _currentSegmentIndex.value) {
           _currentSegmentIndex.value = index
       }
   }
   ```

4. **Auto-scroll**: Compose automatically scrolls to current segment
   ```kotlin
   LaunchedEffect(currentSegmentIndex) {
       lazyListState.animateScrollToItem(currentSegmentIndex)
   }
   ```

5. **Click to Jump**: User can click any segment to jump to that position
   ```kotlin
   fun onSegmentClick(index: Int) {
       val startTime = lrcSyncManager.jumpToSegment(index)
       if (startTime != null) {
           audioManager.seekTo((startTime * 1000).toLong())
       }
   }
   ```

## Progress Tracking

### Automatic Progress Saving

Progress is automatically saved when:
- User pauses playback
- User exits player screen
- Playback completes

```kotlin
private fun saveProgress(timeSpent: Long) {
    val currentPosition = audioManager.currentPosition.value
    val duration = audioManager.duration.value
    val completionPercentage = if (duration > 0) {
        ((currentPosition.toFloat() / duration.toFloat()) * 100f)
    } else 0f
    val isCompleted = completionPercentage >= 95f

    lessonRepository.updateLessonProgress(
        lessonId = currentLesson.id,
        bookId = bookId,
        lastPosition = currentPosition,
        completionPercentage = completionPercentage,
        timeSpent = timeSpent,
        isCompleted = isCompleted
    )
}
```

### Tracked Metrics

- **Last position**: Resume from where you left off
- **Completion percentage**: Track progress through lesson
- **Time spent**: Total study time
- **Completion status**: Not Started, In Progress, Completed

## State Management

### Playback States

```kotlin
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Ended : PlaybackState()
}
```

### State Flow

```
AudioManager (ExoPlayer)
    ↓ StateFlow
PlayerViewModel
    ↓ StateFlow
PlayerScreen (UI)
```

## Navigation Flow

```
HomeScreen
    ↓ [Select Book]
LessonsList
    ↓ [Select Lesson]
PlayerScreen
    ↓ [Back]
LessonsList
    ↓ [Back]
HomeScreen
```

## Performance Optimizations

1. **LazyColumn**: Efficient rendering of long transcripts
2. **Position Polling**: Only updates every 100ms (not every frame)
3. **Auto-scroll**: Uses `animateScrollToItem` for smooth scrolling
4. **State Flow**: Efficient state propagation with Kotlin Flow
5. **Coroutines**: Non-blocking async operations

## Future Enhancements

### Planned Features
- [ ] Background playback service
- [ ] Audio focus handling (pause for calls, other apps)
- [ ] Sleep timer
- [ ] Bookmark positions
- [ ] Repeat/loop controls
- [ ] Gesture controls (swipe to seek)
- [ ] Mini player on home screen
- [ ] Continue playing notification

### Technical Improvements
- [ ] Cache decoded audio for faster playback
- [ ] Preload next lesson
- [ ] Implement proper asset file copying
- [ ] Handle large audio files with streaming
- [ ] Add crash reporting for audio issues

## Known Limitations

1. **File Paths**: Currently uses absolute paths from JSON
   - **Solution**: Copy audio files to assets and update paths

2. **Background Playback**: Not yet implemented
   - **Solution**: Implement Foreground Service

3. **Audio Focus**: Doesn't handle audio focus changes
   - **Solution**: Implement audio focus manager

4. **Large Files**: May have issues with very large audio files
   - **Solution**: Implement streaming or chunking

## Usage Example

```kotlin
// In Compose
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()

    // Control playback
    Button(onClick = { viewModel.play() }) {
        Text("Play")
    }

    // Display position
    Text(formatTime(currentPosition))
}
```

## Dependencies

```gradle
// ExoPlayer (Media3)
implementation("androidx.media3:media3-exoplayer:1.2.0")
implementation("androidx.media3:media3-ui:1.2.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Compose
implementation("androidx.compose.foundation:foundation")
implementation("androidx.compose.material3:material3")
```

## Testing

### Unit Tests
- Test AudioManager state transitions
- Test LRC sync logic with mock data
- Test progress calculation

### Integration Tests
- Test playback with actual audio files
- Test sync with real LRC files
- Test navigation flow

### UI Tests
- Test playback controls
- Test transcript scrolling
- Test speed selector

---

**Status**: ✅ Core functionality complete and working

**Last Updated**: January 5, 2026
