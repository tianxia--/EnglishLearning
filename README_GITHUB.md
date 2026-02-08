# 📚 New Concept English - Learning Companion

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)

A modern, open-source mobile application for learning **New Concept English** (Books 1-4). Designed to provide an immersive learning experience with synchronized audio, interactive exercises, and progress tracking.

> **Note**: This repository contains the source code for the application. Due to copyright restrictions, the original New Concept English audio and text materials are **not** included. Users must provide their own content.

## ✨ Features

### 🎧 Immersive Audio Player
- **Real-time Lyrics Sync**: Follow along with synchronized LRC transcripts that highlight the current sentence.
- **Variable Speed Playback**: Adjust speed from 0.5x to 2.0x to match your listening comfort.
- **Smart/Auto-Scroll**: The transcript automatically scrolls to keep the current sentence in view.
- **Background Playback**: Continue listening even when the app is in the background.

### ✍️ Interactive Learning Modes
- **Dictation/Transcription**: Listen to audio segments and type what you hear. The app provides intelligent feedback, ignoring minor punctuation/capitalization errors.
- **Comprehension Quizzes**: Test your understanding with multiple-choice, true/false, and fill-in-the-blank questions after each lesson.
- **Vocabulary Flashcards**: Master new words using a **Spaced Repetition System (SRS)** that optimizes review intervals for maximum retention.

### 📊 Progress Tracking
- **Detailed Statistics**: Track your study time, lessons completed, and quiz scores.
- **Learning Streak**: Stay motivated by tracking your daily learning streak.
- **Data Persistence**: All progress is saved locally using Room Database.

## 📱 Tech Stack (Android)

This project is built with modern Android development practices:

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Design)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Asynchronous**: Coroutines + Kotlin Flow
- **Local Storage**: [Room](https://developer.android.com/training/data-storage/room)
- **Media Playback**: [ExoPlayer (Media3)](https://developer.android.com/media/media3)
- **Navigation**: [Compose Navigation](https://developer.android.com/guide/navigation)

## 🛠️ Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 34

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/yourusername/new-concept-english-app.git
    cd new-concept-english-app
    ```

2.  **Import Assets**
    To respect copyright, you need to provide the learning materials.
    - Place your `book1` to `book4` content folders in `android-native-app/app/src/main/assets/`.
    - Ensure your `indexed_lessons.json` is present.
    
    *Structure:*
    ```text
    app/src/main/assets/
    ├── indexed_lessons.json
    ├── book1/
    ├── book2/
    ├── ...
    ```

3.  **Build and Run**
    - Open the project in Android Studio.
    - Sync Gradle.
    - Run on an emulator or physical device.

## 🗺️ Roadmap

- [x] **Android App (Core)**: Complete audio player, quizzes, and flashcards.
- [ ] **iOS Version**: Native iOS app using SwiftUI.
- [ ] **Cross-Platform**: Potential Flutter/React Native ports.
- [ ] **Cloud Sync**: Optional cloud backup for progress.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📄 License

Distributed under the GNU General Public License v3.0. See `LICENSE` for more information.

## 🙏 Acknowledgments

- **New Concept English**: For the excellent learning material designed by L.G. Alexander.
- **Open Source Community**: For the amazing libraries and tools used in this project.
