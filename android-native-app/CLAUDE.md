# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**New Concept English Learning App** - An Android native application built with Jetpack Compose for learning English through audio lessons, vocabulary flashcards, quizzes, and dictation exercises.

**Tech Stack:**
- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger Hilt)
- **Database**: Room
- **Audio**: ExoPlayer (Media3)
- **Async**: Coroutines + StateFlow
- **Build**: Gradle with Kotlin DSL

## Build and Test Commands

### Building the Project
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.englishlearning.data.repository.LessonRepositoryTest"
```

### Code Quality
```bash
# No linting configured currently - add ktlint or detekt if needed
```

## High-Level Architecture

### Layer Structure
```
com.englishlearning/
├── newconcept/          # Application entry point (@HiltAndroidApp)
│   └── ui/MainActivity  # Single-activity host
├── ui/                  # Presentation Layer (Compose UI)
│   ├── screens/         # Screen composables with ViewModels
│   ├── components/      # Reusable UI components
│   ├── navigation/      # Navigation Compose setup
│   └── theme/           # Material 3 theming
├── data/                # Data Layer
│   ├── local/           # Room database (DAOs, Entities)
│   ├── model/           # Data classes (Kotlin serialization)
│   ├── repository/      @Singleton repository classes
│   ├── cache/           # Asset caching
│   ├── preferences/     # DataStore preferences
│   └── translation/     # Translation API integration
├── domain/              # Domain Layer (minimal - use cases/interfaces)
├── service/             # Foreground services (AudioPlayerService)
└── di/                  # Hilt modules
```

### Key Architecture Patterns

1. **MVVM with Compose**: Each major screen has a ViewModel (`@HiltViewModel`) that exposes `StateFlow`/`MutableStateFlow` for reactive UI updates

2. **Hilt Dependency Injection**:
   - Application: `@HiltAndroidApp` on `NewConceptEnglishApp`
   - Activities: `@AndroidEntryPoint`
   - ViewModels: `@HiltViewModel`
   - Modules: `DatabaseModule`, `TranslationEntryPointModule`
   - Repositories/Services: `@Singleton` scope

3. **Repository Pattern**:
   - Repositories are the single source of truth for data
   - Inject DAOs for database access
   - Use `withContext(Dispatchers.IO)` for database operations

4. **Navigation Compose**:
   - Typed navigation routes in `NavRoute.kt`
   - Bottom navigation with 4 tabs: Home, Progress, Vocabulary, Settings
   - Deep linking support for `bookId` and `lessonId` parameters

### State Management Conventions

- ViewModels expose immutable `StateFlow<T>` for read-only state
- Use `MutableStateFlow<T>` privately in ViewModels
- Compose screens collect state via `collectAsState()`
- Use `LaunchedEffect` for one-time initialization

### Database Conventions

- **Room** for local persistence
- Entities use `@Entity` annotation
- DAOs are injected into repositories
- All database operations must be on `Dispatchers.IO`
- Database migration uses `fallbackToDestructiveMigration()` (development only)

### Asset Loading

- Lesson content (JSON, LRC, MP3) stored in `assets/`
- `LessonLoader` handles parsing and caching
- `CacheManager` manages in-memory asset cache
- Use `ContentRepository` for accessing lesson data

## Audio Playback Architecture

The app uses **ExoPlayer (Media3)** for audio playback with custom LRC synchronization:

- **AudioManager**: Wraps ExoPlayer, handles playback controls
- **LrcSyncManager**: Manages LRC transcript synchronization with playback position
- **AudioPlayerService**: Foreground service for background playback (partially implemented)
- **AudioServiceManager**: Manages service lifecycle

**Important**: Time unit inconsistencies exist between components (seconds vs milliseconds). Always verify units when working with audio position data.

## Known Issues and Technical Debt

See `PROJECT_ANALYSIS.md` for detailed analysis. Key issues:

1. **LRC Sync Accuracy**: Simple linear search in `LrcSyncManager` may not be precise enough
2. **Time Unit Inconsistency**: Mix of seconds (Double) and milliseconds (Long) in audio components
3. **Incomplete Domain Layer**: Use cases are minimally implemented
4. **Limited Testing**: No comprehensive test suite

## Adding New Features

### New Screen
1. Create composable in `ui/screens/[feature]/[Feature]Screen.kt`
2. Create ViewModel in `ui/screens/[feature]/[Feature]ViewModel.kt` with `@HiltViewModel`
3. Add navigation route in `ui/navigation/NavRoute.kt`
4. Add route handling in `ui/navigation/AppNavigation.kt`

### New Repository
1. Create repository class in `data/repository/` with `@Singleton` and `@Inject constructor`
2. Inject required DAOs or dependencies
3. Suspend functions for database operations on `Dispatchers.IO`
4. Return StateFlow or simple types as appropriate

### Database Changes
1. Update Entity in `data/local/database/`
2. Update corresponding DAO in `data/local/dao/`
3. Update `AppDatabase` `version` and add migration strategy
4. Update `DatabaseModule` if adding new DAOs

## Coding Conventions

- **Kotlin-first**: Write idiomatic Kotlin, avoid Java patterns
- **Null Safety**: Use `?` for nullable types, avoid `!!` non-null assertions
- **Coroutines**: Use `suspend` functions for async work, `Dispatchers.IO` for database/network
- **Compose**: Follow state hoisting principles - keep state in ViewModels, pass state down as parameters
- **Naming**: Use clear, descriptive names following Kotlin conventions
- **File Structure**: One public class per file, file name matches class name

## Module and Package Structure

- `com.englishlearning.newconcept` - Application and main activity
- `com.englishlearning.ui.*` - All UI-related code
- `com.englishlearning.data.*` - Data layer (repositories, database, models)
- `com.englishlearning.domain.*` - Business logic (limited implementation)
- `com.englishlearning.di.*` - Hilt dependency injection modules
- `com.englishlearning.service.*` - Android services (foreground service for audio)

## Translation API

The app includes a translation service with entrypoint-based dependency injection. See `TRANSLATION_API_GUIDE.md` for details on the translation system architecture.
