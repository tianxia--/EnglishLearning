# New Concept English - Multi-Platform Learning App

A comprehensive English learning application featuring **New Concept English** (Books 1-4) with synchronized audio and transcripts, built for multiple mobile platforms.

## 🎯 Project Overview

This app provides an interactive learning experience for New Concept English, a popular English learning series in China. Features include:

- ✅ **560 lessons** from all 4 New Concept English books
- 🎧 **Synchronized audio playback** with LRC transcript display
- 📝 **Transcription exercises** - listen and type what you hear
- ❓ **Comprehension quizzes** - test your understanding
- 📚 **Vocabulary builder** - spaced repetition flashcard system
- 📊 **Progress tracking** - monitor your learning journey
- 🌙 **Dark/Light theme** support

## 📱 Platforms

This project is being built for **4 mobile platforms**:

| Platform | Status | Technology |
|----------|--------|------------|
| 🤖 Android | ✅ **Core Features Complete** | Kotlin + Jetpack Compose |
| 🍎 iOS Native | ⏳ Planned | Swift + SwiftUI |
| 📱 Flutter | ⏳ Planned | Dart (Cross-platform) |
| ⚛️ React Native | ⏳ Planned | TypeScript (Cross-platform) |

## 📂 Project Structure

```
/Users/pengfei.chen/Desktop/privateWork/
├── shared-content/              # Shared learning content
│   ├── indexed_lessons.json     # Master index of all lessons
│   ├── book1/                   # Book 1 lessons (72 lessons)
│   ├── book2/                   # Book 2 lessons (96 lessons)
│   ├── book3/                   # Book 3 lessons (60 lessons)
│   ├── book4/                   # Book 4 lessons (48 lessons)
│   └── index_content.py         # Content indexer script
│
├── android-native-app/          # ✅ Android app (In Progress)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/englishlearning/
│   │   │   │   ├── data/        # Data layer
│   │   │   │   │   ├── model/   # Data models
│   │   │   │   │   ├── local/   # Room database
│   │   │   │   │   └── repository/ # Repositories
│   │   │   │   ├── ui/          # UI layer (Compose)
│   │   │   │   │   ├── screens/ # Screens
│   │   │   │   │   ├── components/ # Reusable components
│   │   │   │   │   └── theme/   # Theme
│   │   │   │   └── di/          # Dependency Injection (Hilt)
│   │   │   └── res/             # Resources
│   │   └── build.gradle.kts     # Gradle build config
│   └── build.gradle.kts         # Project build config
│
├── flutter-app/                 # Flutter app (To be created)
├── react-native-app/            # React Native app (To be created)
├── ios-native-app/              # iOS Native app (To be created)
│
├── 新概念课文1-4PDF/             # Original PDF textbooks
└── 英音/                         # Original audio + LRC files
```

## ✅ What's Been Completed

### 1. Content Indexing ✅
- **276 lessons** indexed from New Concept English Books 1-4
- **LRC parser** that extracts synchronized transcripts
- Each lesson includes:
  - Audio file path (.MP3)
  - Synchronized text segments with timestamps
  - Lesson metadata (title, duration, level)

### 2. Android App Architecture ✅

#### Data Layer
- ✅ **Data Models**: `Lesson`, `Book`, `Segment`, `Vocabulary`, `Quiz`, `UserProgress`
- ✅ **Room Database**:
  - `LessonProgressEntity` - Track lesson completion
  - `UserProgressEntity` - Track overall progress
  - `VocabularyProgressEntity` - Track vocabulary mastery
  - DAOs for all entities
- ✅ **Repositories**:
  - `ContentRepository` - Load lessons from assets
  - `LessonRepository` - Manage progress and vocabulary
  - `LessonLoader` - Asset file loading

#### Dependency Injection
- ✅ **Hilt** setup with modules:
  - `DatabaseModule` - Provide Room database
  - `RepositoryModule` - Provide repositories

#### UI Layer (Jetpack Compose)
- ✅ **Theme** - Material 3 design with dark/light mode
- ✅ **Navigation** - Compose Navigation setup
- ✅ **Home Screen** - Browse books and lessons
- ✅ **Lessons List Screen** - Display lessons with progress
- ✅ **Player Screen** - Audio player with synchronized transcript
- ✅ **Components**:
  - `BookCard` - Display book information
  - `LessonListItem` - Display lesson with progress
  - `TranscriptSegment` - Synchronized transcript display
  - `PlayerControls` - Playback controls and speed adjustment
- ✅ **ViewModels**:
  - `HomeViewModel` - Book management
  - `LessonsViewModel` - Lesson list management
  - `PlayerViewModel` - Player state management

### 3. Build Configuration ✅
- ✅ Gradle build files (Kotlin DSL)
- ✅ Dependencies configured:
  - Jetpack Compose for UI
  - ExoPlayer for audio playback
  - Room for local database
  - Hilt for dependency injection
  - Material 3 for design

## 🚧 What's Next

### Immediate Priorities

#### 1. Audio Player Implementation (Android) ✅
- [x] Create `AudioManager` with ExoPlayer
- [x] Implement playback controls (play, pause, seek, speed)
- [x] Sync transcript with audio playback (`LrcSyncManager`)
- [x] Create `PlayerScreen` UI with synchronized transcript
- [x] Implement progress tracking
- [x] Handle playback state management
- [x] Background playback service (Basic implementation via ExoPlayer)
- [ ] Handle audio focus and interruptions

#### 2. Lesson Loading ✅
- [x] Implement `LessonLoader` for asset reading
- [x] Load lessons from `ContentRepository`
- [x] Display lessons in `LessonsList`
- [x] Navigate from book selection to lesson list
- [x] Implement lesson list UI with progress indicators

#### 3. Core Features
- [x] **Audio Player** - Full playback with controls
- [x] **LRC Synchronization** - Real-time transcript sync
- [x] **Progress Tracking** - Database persistence
- [x] **Navigation** - Screen navigation setup
- [x] **Transcription Exercise** - Listen and type mode
- [x] **Comprehension Quizzes** - Multiple choice questions
- [x] **Vocabulary System** - Flashcard interface
- [x] **Progress Dashboard** - Statistics and achievements

#### 4. Polish & Testing
- [x] Add loading states and error handling (Added `LoadingAnimation`)
- [x] Implement dark theme fully (Fixed `Color.kt` tokens)
- [x] Add animations and transitions
- [ ] Test on Android device/emulator

### Future Platforms

#### iOS Native (Swift/SwiftUI)
- [ ] Initialize Xcode project
- [ ] Implement similar architecture to Android
- [ ] Use AVFoundation for audio
- [ ] CoreData for local storage

#### Flutter (Dart)
- [ ] Create Flutter project
- [ ] Share data models and business logic
- [ ] Cross-platform UI with Flutter widgets

#### React Native (TypeScript)
- [ ] Initialize React Native project
- [ ] Implement with TypeScript
- [ ] Use React Navigation and Paper UI

## 🛠️ Technology Stack

### Android (Current Focus)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **Audio**: ExoPlayer (Media3)
- **Database**: Room
- **DI**: Hilt
- **Async**: Coroutines + Flow

### Content
- **Audio Format**: MP3
- **Transcript Format**: LRC (synchronized lyrics)
- **Data Format**: JSON
- **Scripting**: Python 3 (for content indexing)

## 📦 Dependencies

### Android
```gradle
// Core
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0

// Compose
androidx.compose:compose-bom:2024.02.00
androidx.compose.material3:material3
androidx.navigation:navigation-compose:2.7.6

// Audio
androidx.media3:media3-exoplayer:1.2.0
androidx.media3:media3-ui:1.2.0

// Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// DI
com.google.dagger:hilt-android:2.50

// Others
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
androidx.datastore:datastore-preferences:1.0.0
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK API 34
- Kotlin 1.9.20

### Build Instructions

1. **Clone/Download this project**

2. **Open in Android Studio**
   ```bash
   open /Users/pengfei.chen/Desktop/privateWork/android-native-app
   ```

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync Gradle
   - Click "Sync Now"

4. **Copy Content to Assets**
   ```bash
   mkdir -p android-native-app/app/src/main/assets/
   cp -r shared-content/indexed_lessons.json android-native-app/app/src/main/assets/
   cp -r shared-content/book1 android-native-app/app/src/main/assets/
   cp -r shared-content/book2 android-native-app/app/src/main/assets/
   cp -r shared-content/book3 android-native-app/app/src/main/assets/
   cp -r shared-content/book4 android-native-app/app/src/main/assets/
   ```

5. **Copy Audio Files** (Optional - for offline playback)
   ```bash
   # You can either copy the actual MP3 files or use the paths in the JSON
   # For now, the app uses absolute paths from the JSON files
   # TODO: Implement proper asset copying for audio files
   ```

6. **Run the App**
   - Connect Android device or start emulator
   - Click Run button in Android Studio
   - Or use command line:
     ```bash
     cd android-native-app
     ./gradlew installDebug
     ```

## 📊 Content Summary

| Book | Level | Lessons | Focus |
|------|-------|---------|-------|
| Book 1 | A1-A2 | 72 | First Things First - Beginner |
| Book 2 | A2-B1 | 96 | Practice and Progress - Pre-intermediate |
| Book 3 | B1-B2 | 60 | Developing Skills - Intermediate |
| Book 4 | B2-C1 | 48 | Fluency in English - Upper-intermediate |
| **Total** | | **276** | |

## 🎓 Learning Features

### 1. Audio Player
- Play/Pause/Stop controls
- Adjustable playback speed (0.5x - 2x)
- Skip forward/backward (10s, 30s)
- Progress bar with seeking
- Background playback

### 2. Synchronized Transcripts
- Real-time transcript display synced with audio
- Highlight current segment
- Auto-scroll as audio plays
- Click segment to jump to position

### 3. Transcription Exercises
- Listen to audio segment
- Type what you hear
- Real-time comparison with correct text
- Error highlighting
- Hint system
- Score tracking

### 4. Comprehension Quizzes
- Multiple choice questions
- True/False questions
- Fill-in-the-blank exercises
- Instant feedback with explanations
- Performance tracking

### 5. Vocabulary Builder
- Extract words from context
- Spaced repetition system
- Flashcard review
- Mastery tracking
- Search and filter

### 6. Progress Tracking
- Lesson completion status
- Time spent studying
- Streak tracking
- Statistics dashboard
- Export progress data

## 📝 Code Guidelines

This project follows the global development guidelines in `~/CLAUDE.md`:

- **Clean Architecture** - Separation of concerns
- **MVVM Pattern** - Model-View-ViewModel
- **Dependency Injection** - Hilt for Android
- **Kotlin Best Practices** - Idiomatic Kotlin code
- **Material Design** - Material 3 guidelines
- **Error Handling** - Proper exception handling
- **Testing** - Unit and integration tests

## 🤝 Contributing

This is a personal learning project, but suggestions and improvements are welcome!

## 📄 License

This project is for personal learning purposes. The New Concept English content is copyrighted material.

## 🙏 Acknowledgments

- **New Concept English** by L.G. Alexander
- Original content from the user's collection
- Android and Jetpack libraries by Google
- Material Design guidelines

---

**Status**: 🚧 **In Active Development**

**Last Updated**: January 5, 2026

**Version**: 0.1.0 (Alpha)

**Platform**: Android (Primary), iOS/Flutter/React Native (Planned)
