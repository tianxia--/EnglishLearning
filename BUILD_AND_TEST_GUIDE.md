# Build and Test Guide - New Concept English App

## 📋 Prerequisites Check

Before building, ensure you have:

### Required Software
- ✅ **Android Studio** Hedgehog (2023.1.1) or later
- ✅ **JDK 17** or later
- ✅ **Android SDK** API 34
- ✅ **Gradle 8.2** (included with project)

### Check Your Installation
```bash
# Check Java version
java -version
# Should show: java version "17.x.x" or later

# Check Android SDK
echo $ANDROID_HOME
# Should show path to Android SDK

# Check for adb (Android Debug Bridge)
adb version
# Should show Android Debug Bridge version
```

---

## 🔧 Setup Instructions

### Step 1: Copy Content to Assets

The app needs the lesson content in the assets folder:

```bash
cd /Users/pengfei.chen/Desktop/privateWork

# Create assets directory
mkdir -p android-native-app/app/src/main/assets/

# Copy content files
cp shared-content/indexed_lessons.json android-native-app/app/src/main/assets/
cp -r shared-content/book1 android-native-app/app/src/main/assets/
cp -r shared-content/book2 android-native-app/app/src/main/assets/
cp -r shared-content/book3 android-native-app/app/src/main/assets/
cp -r shared-content/book4 android-native-app/app/src/main/assets/

# Verify
ls android-native-app/app/src/main/assets/
# Should show: indexed_lessons.json, book1/, book2/, book3/, book4/
```

### Step 2: Open in Android Studio

```bash
# Open Android Studio and open the project
open -a "Android Studio" android-native-app

# Or use Android Studio CLI (if configured)
studio android-native-app
```

### Step 3: Gradle Sync

When Android Studio opens:
1. Wait for automatic Gradle sync
2. If prompted, click "Sync Now"
3. Wait for dependency download (may take a few minutes)

---

## 📱 Building the App

### Option 1: Using Android Studio (Recommended)

1. **Select Build Variant**
   - View → Build Variants
   - Select `debug` for testing

2. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for build completion

3. **Find APK**
   - Location: `android-native-app/app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Using Command Line

```bash
cd android-native-app

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Find APK
ls app/build/outputs/apk/debug/app-debug.apk
```

---

## 📲 Installing on Device

### Method 1: Via USB (Recommended)

1. **Enable Developer Options** on your Android device:
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back → System → Developer Options
   - Enable "USB Debugging"

2. **Connect Device** via USB

3. **Install APK**:
```bash
# Install via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or from Android Studio
# Run → Run 'app'
```

### Method 2: Via Emulator

1. **Create Emulator** in Android Studio:
   - Tools → Device Manager
   - Create Device
   - Select device (e.g., Pixel 5)
   - Select system image (API 34)
   - Finish

2. **Run App**:
   - Select emulator
   - Click Run button in Android Studio

---

## 🧪 Testing Guide

### Test Checklist

#### 1. Home Screen Tests
```
✅ App launches successfully
✅ All 4 books are displayed
✅ Book cards show correct info (title, level, lesson count)
✅ Tap book card → Opens lessons list
✅ Bottom navigation shows all tabs
```

#### 2. Lessons List Tests
```
✅ Lessons load correctly
✅ Lesson count matches (Book 1: 72, Book 2: 96, etc.)
✅ Progress indicators show correctly
✅ Lesson titles display
✅ Duration shows in minutes
✅ Back button returns to book list
✅ Tap lesson → Opens audio player
```

#### 3. Audio Player Tests
```
✅ Audio player screen opens
✅ Lesson title and number display
✅ Transcript segments load
✅ Play button starts audio
✅ Pause button works
✅ Progress slider shows position
✅ Seek to position works
✅ Speed selector shows options (0.5x - 2x)
✅ Speed change works
✅ Current segment highlights
✅ Transcript auto-scrolls
✅ Tap segment → Jumps to position
✅ Toggle transcript visibility
✅ Back button returns to lessons
```

#### 4. Transcription Exercise Tests
```
✅ Transcription screen opens (via menu)
✅ Listen button works (simulated)
✅ Text input accepts typing
✅ Submit button validates input
✅ Correct answer → Green checkmark
✅ Incorrect answer → Shows differences
✅ Differences highlight correctly (wrong/missing/extra)
✅ Hint button reveals first letters
✅ Reveal button shows full answer
✅ Next button advances to next segment
✅ Score updates correctly
✅ Progress bar updates
✅ Complete screen shows final score
✅ Retry button restarts exercise
```

#### 5. Quiz Tests
```
✅ Quiz screen opens (via menu)
✅ Multiple choice questions display
✅ Answer options show correctly
✅ Select answer → Highlights
✅ Submit validates answer
✅ Correct → Green checkmark
✅ Incorrect → Red X, shows correct answer
✅ Explanation displays (if provided)
✅ Score updates after each question
✅ Progress indicator works
✅ Next question advances
✅ Final score screen shows:
   - Total questions
   - Correct count
   - Final score percentage
   - Performance message
✅ Retry button restarts quiz
```

#### 6. Vocabulary Flashcards Tests
```
✅ Flashcards screen opens (bottom nav)
✅ "No vocabulary" message if empty (expected initially)
✅ After studying lessons → Flashcards load
✅ Cards show word on front
✅ Tap card → Flips to show definition
✅ "Still Learning" button:
   - Removes from current session
   - Adds to end of queue
   - Updates incorrect count
✅ "Know It" button:
   - Advances to next card
   - Updates correct count
✅ Progress updates correctly
✅ Session complete screen shows:
   - Total cards studied
   - Correct count
   - Incorrect count
   - Accuracy percentage
✅ Study again button restarts session
```

---

## 🐛 Common Issues and Fixes

### Issue 1: Gradle Sync Fails

**Error**: "Could not resolve dependencies"

**Solution**:
```bash
# Clean and rebuild
cd android-native-app
./gradlew clean
./gradlew build --refresh-dependencies
```

### Issue 2: Content Not Loading

**Error**: "No lessons found" or blank screens

**Solution**:
```bash
# Verify assets are copied
ls android-native-app/app/src/main/assets/book1/

# Should show lesson JSON files
# If empty, recopy content
cp -r shared-content/book* android-native-app/app/src/main/assets/
```

### Issue 3: Audio Not Playing

**Error**: "Failed to load audio"

**Reason**: Audio files use absolute paths from JSON

**Temporary Workaround**:
- The audio player works but needs proper file paths
- For now, you'll see the UI and controls
- Audio playback needs file path updates

### Issue 4: Build Errors

**Error**: "Unresolved reference" or compilation errors

**Solution**:
```bash
# Clean build
./gradlew clean

# Invalidate caches in Android Studio
# File → Invalidate Caches → Invalidate and Restart
```

### Issue 5: Out of Memory Error

**Error**: "Java heap space"

**Solution**:
```bash
# Increase memory in gradle.properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

---

## 📊 Testing Results Template

### Test Session Notes

**Date**: _____________
**Device**: _____________
**Android Version**: _____________

| Feature | Status | Notes |
|---------|--------|-------|
| Home Screen | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Lessons List | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Audio Player | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Transcription | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Quiz | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Flashcards | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Navigation | ⬜ Not Tested / ✅ Pass / ❌ Fail | |
| Progress Save | ⬜ Not Tested / ✅ Pass / ❌ Fail | |

**Overall Rating**: ⬜ ⬜ ⬜ ⬜ ⬜ (1-5 stars)

**Issues Found**:
1. ________________________________________
2. ________________________________________
3. ________________________________________

---

## 🎬 Quick Test Workflow

### 5-Minute Smoke Test

1. **Launch App** (30 seconds)
   - App opens to home screen
   - 4 books visible

2. **Navigate to Lessons** (30 seconds)
   - Tap Book 1
   - Lessons list appears
   - 72 lessons shown

3. **Open Audio Player** (1 minute)
   - Tap first lesson
   - Player screen opens
   - Controls visible
   - Transcript shows

4. **Try Transcription** (1 minute)
   - Back to lessons
   - Tap menu on lesson
   - Select "Transcription"
   - Exercise screen opens
   - Type something, submit

5. **Try Quiz** (1 minute)
   - Back to lessons
   - Tap menu on lesson
   - Select "Quiz"
   - Quiz screen opens
   - Answer question

6. **Try Flashcards** (1 minute)
   - Tap bottom nav "Flashcards"
   - Flashcard screen opens
   - Shows "No vocabulary yet" (expected)

**Total**: 5 minutes

---

## 🚀 Next Steps After Testing

### If Tests Pass
1. Add actual audio files to assets
2. Add more quiz questions
3. Test on multiple devices
4. Create release build

### If Tests Fail
1. Note error messages
2. Check logcat:
```bash
adb logcat | grep "englishlearning"
```
3. Review error logs
4. Fix issues and rebuild

---

## 📝 Build Summary

### Project Statistics
- **Total Kotlin Files**: 35+
- **Total Lines of Code**: ~5,000+
- **Features**: 6 major features
- **Screens**: 6 main screens
- **Dependencies**: 15+ libraries

### Features Ready to Test
1. ✅ Home Screen with book browser
2. ✅ Lessons List with progress
3. ✅ Audio Player with LRC sync
4. ✅ Transcription Exercises
5. ✅ Comprehension Quizzes
6. ✅ Vocabulary Flashcards

---

## 🎓 Testing Best Practices

### Manual Testing Tips
- Test on real device (not just emulator)
- Test with different Android versions
- Test with different screen sizes
- Test offline behavior
- Test navigation flows
- Test state retention (rotate screen, background app)

### What to Look For
- **UI Polish**: Spacing, colors, fonts
- **Performance**: Smooth animations, no lag
- **Crashes**: App should never crash
- **Data Loss**: Progress should save
- **Navigation**: Back button should work
- **Feedback**: Loading states, error messages

---

## 📞 Getting Help

### If Build Fails
1. Check Android Studio log
2. Check gradle console output
3. Check logcat: `adb logcat`
4. Review error messages

### Common Commands
```bash
# Check connected devices
adb devices

# Install APK
adb install app-debug.apk

# Uninstall app
adb uninstall com.englishlearning.newconcept

# View logs
adb logcat | grep "englishlearning"

# Clear app data
adb shell pm clear com.englishlearning.newconcept

# Force stop app
adb shell am force-stop com.englishlearning.newconcept
```

---

**Good luck with testing! 🚀**

Once you test the app, let me know what issues you find and I'll help fix them!
