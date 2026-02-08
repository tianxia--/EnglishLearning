# Testing Checklist for New Concept English App

## 📱 Pre-Build Checklist

### Environment Setup
- [ ] Android Studio installed (version Hedgehog 2023.1.1 or later)
- [ ] JDK 17 or later installed
- [ ] Android SDK API 34 installed
- [ ] Device or emulator available
- [ ] USB debugging enabled (if using device)

### Content Files
- [ ] `indexed_lessons.json` copied to assets
- [ ] `book1/` folder copied to assets (72 lessons)
- [ ] `book2/` folder copied to assets (96 lessons)
- [ ] `book3/` folder copied to assets (60 lessons)
- [ ] `book4/` folder copied to assets (48 lessons)

---

## 🧪 Feature Testing Checklist

### 1. Home Screen (首页)
#### Visual Elements
- [ ] App title displays: "新概念英语"
- [ ] 4 book cards visible
- [ ] Bottom navigation bar shows 4 tabs
- [ ] Books tab selected (home icon highlighted)

#### Book Cards
- [ ] Book 1: "New Concept English Book 1"
  - [ ] Level: "A1-A2"
  - [ ] Shows "72 lessons"
- [ ] Book 2: "New Concept English Book 2"
  - [ ] Level: "A2-B1"
  - [ ] Shows "96 lessons"
- [ ] Book 3: "New Concept English Book 3"
  - [ ] Level: "B1-B2"
  - [ ] Shows "60 lessons"
- [ ] Book 4: "New Concept English Book 4"
  - [ ] Level: "B2-C1"
  - [ ] Shows "48 lessons"

#### Interactions
- [ ] Tap book card → Opens lessons list
- [ ] Tap "Books" tab → Shows books
- [ ] Tap "Progress" tab → Shows progress (placeholder)
- [ ] Tap "Flashcards" tab → Opens flashcard screen

---

### 2. Lessons List (课程列表)
#### Visual Elements
- [ ] Back button visible in top bar
- [ ] Book title shows correctly
- [ ] Book description displays
- [ ] Level and lesson count shown in card
- [ ] Lessons list scrollable
- [ ] Lesson items displayed

#### Lesson Items
For each lesson (test first 3-5):
- [ ] Lesson number visible
- [ ] Lesson title visible
- [ ] Duration shows in minutes
- [ ] Progress indicator visible:
  - [ ] Gray circle = Not Started
  - [ ] Orange icon = In Progress
  - [ ] Green checkmark = Completed
- [ ] Three-dot menu button visible

#### Interactions
- [ ] Tap back button → Returns to home
- [ ] Tap lesson title → Opens audio player
- [ ] Tap three-dot menu → Shows options:
  - [ ] "Audio Player" option
  - [ ] "Transcription" option
  - [ ] "Quiz" option

---

### 3. Audio Player (音频播放器)
#### Visual Elements
- [ ] Back button visible
- [ ] Eye icon for toggling transcript
- [ ] Lesson number and title displayed
- [ ] Transcript visible (if enabled)
- [ ] Progress slider visible
- [ ] Time labels show position/duration
- [ ] Playback controls visible:
  - [ ] Speed selector (shows "1.0x")
  - [ ] Skip backward button (⏮)
  - [ ] Play/Pause button (large circular button)
  - [ ] Skip forward button (⏭)
  - [ ] Stop button (⏹)

#### Transcript Display
- [ ] Transcript segments displayed as cards
- [ ] Current segment highlighted (blue background)
- [ ] Normal segments white background
- [ ] Scrollable if transcript is long
- [ ] Smooth scroll to current segment

#### Interactions
- [ ] Tap play → Button changes to pause icon
- [ ] Tap pause → Button changes to play icon
- [ ] Tap backward → Skips back 10 seconds
- [ ] Tap forward → Skips forward 10 seconds
- [ ] Tap speed selector → Shows dropdown:
  - [ ] 0.5x selectable
  - [ ] 0.75x selectable
  - [ ] 1.0x selectable
  - [ ] 1.25x selectable
  - [ ] 1.5x selectable
  - [ ] 2.0x selectable
- [ ] Select speed → Changes immediately
- [ ] Drag progress slider → Seeks to position
- [ ] Tap transcript segment → Jumps audio to that segment
- [ ] Tap eye icon → Toggles transcript visibility
- [ ] Tap back button → Returns to lessons list

---

### 4. Transcription Exercise (听写练习)
#### Visual Elements
- [ ] Back button visible
- [ ] Score displayed in top right
- [ ] Progress bar visible
- [ ] "Segment X of Y" text shows
- [ ] Large card with:
  - [ ] Headphone icon
  - [ ] "Listen to the audio" text
  - [ ] "Type what you hear" text
  - [ ] "Play Audio" button
- [ ] Hint card (when enabled):
  - [ ] Lightbulb icon
  - [ ] "Hint" label
  - [ ] First letters of words shown
- [ ] Text input field:
  - [ ] Label: "Type what you heard"
  - [ ] Placeholder: "Start typing..."
  - [ ] Multiple lines visible
- [ ] Action buttons:
  - [ ] "Hint" button
  - [ ] "Reveal" button
  - [ ] "Submit" button

#### Interactions - Exercise Phase
- [ ] Tap "Play Audio" → Audio plays (simulated)
- [ ] Type in text field → Text appears
- [ ] Tap "Hint" → Shows first letters
- [ ] Tap "Reveal" → Shows correct answer
- [ ] Tap "Submit" with empty input → Nothing happens
- [ ] Tap "Submit" with text → Validates answer

#### Interactions - Correct Answer
- [ ] Green checkmark icon shows
- [ ] "Correct! 🎉" text shows
- [ ] Score card shows with percentage
- [ ] Your answer displayed
- [ ] Correct answer displayed in green
- [ ] "Next" button visible
- [ ] Tap "Next" → Advances to next segment

#### Interactions - Incorrect Answer
- [ ] Red X icon shows
- [ ] "Not Quite" text shows
- [ ] Score card shows with percentage
- [ ] Your answer displayed in red
- [ ] Correct answer displayed in green
- [ ] Differences highlighted:
  - [ ] ❌ Wrong words shown
  - [ ] ➖ Missing words shown
  - [ ] ➕ Extra words shown
- [ ] "Retry" button visible
- [ ] "Next" button visible
- [ ] Tap "Retry" → Clears input, stays on segment
- [ ] Tap "Next" → Advances to next segment

#### Completion Screen
- [ ] Trophy icon visible
- [ ] "Exercise Complete! 🎉" text shows
- [ ] Final score displayed:
  - [ ] Percentage shows
  - [ ] X of Y correct
- [ ] "Try Again" button visible
- [ ] "Back to Lessons" button visible
- [ ] Tap "Try Again" → Restarts exercise
- [ ] Tap "Back to Lessons" → Returns to lessons list

---

### 5. Comprehension Quiz (理解测验)
#### Visual Elements
- [ ] Back button visible
- [ ] Progress bar visible
- [ ] "Question X of Y" text shows
- [ ] Question card with:
  - [ ] Badge showing question type
  - [ ] Question text
- [ ] Answer options displayed (4 for multiple choice):
  - [ ] A. First option with circle
  - [ ] B. Second option with circle
  - [ ] C. Third option with circle
  - [ ] D. Fourth option with circle
- [ ] Selected option has blue checkmark
- [ ] "Submit Answer" button visible (disabled when no selection)

#### Interactions - Question Phase
- [ ] Tap option → Selects it (blue checkmark)
- [ ] Tap another option → Changes selection
- [ ] Tap "Submit" with no selection → Button disabled
- [ ] Tap "Submit" with selection → Validates answer

#### Interactions - Correct Answer
- [ ] Green checkmark icon (large)
- [ ] "Correct! 🎉" text shows
- [ ] Score card shows percentage
- [ ] Your answer displayed in green
- [ ] Explanation card shows (if provided):
  - [ ] Lightbulb icon
  - [ ] "Explanation" label
  - [ ] Explanation text
- [ ] "Next Question" button visible
- [ ] Tap "Next" → Advances to next question

#### Interactions - Incorrect Answer
- [ ] Red X icon (large)
- [ ] "Incorrect" text shows
- [ ] Score card shows percentage
- [ ] Your answer displayed in red
- [ ] Correct answer displayed in green (in separate card)
- [ ] Explanation card shows (if provided)
- [ ] "Next Question" button visible
- [ ] Tap "Next" → Advances to next question

#### Completion Screen
- [ ] Trophy icon visible (color based on score)
- [ ] "Quiz Complete! 🎉" text shows
- [ ] Final score displayed:
  - [ ] Percentage shows
  - [ ] X out of Y correct
- [ ] Performance message shows:
  - [ ] 80%+: "Excellent work! 🌟"
  - [ ] 60-79%: "Good job! Keep practicing! 💪"
  - [ ] <60%: "Keep studying! You'll improve! 📚"
- [ ] Stats row shows:
  - [ ] Correct count (green)
  - [ ] Learning count (red)
- [ ] "Try Again" button visible
- [ ] "Back to Lessons" button visible
- [ ] Tap "Try Again" → Restarts quiz
- [ ] Tap "Back to Lessons" → Returns to lessons list

---

### 6. Vocabulary Flashcards (词汇卡片)
#### Initial State (No Vocabulary)
- [ ] Back button visible
- [ ] Book icon visible
- [ ] "No vocabulary yet" text shows
- [ ] Instruction text shows
- [ ] "Back to Lessons" button visible
- [ ] Tap "Back to Lessons" → Returns to lessons list

#### Study Session
#### Visual Elements
- [ ] Back button visible
- [ ] Score displayed in top right (X/Y format)
- [ ] Progress bar visible
- [ ] "Card X of Y" text shows
- [ ] Flashcard visible:
  - [ ] Front side: Word prominently displayed
  - [ ] Book icon
  - [ ] "Tap to see meaning" text
  - [ ] Blue background
- [ ] Instruction text below card:
  - [ ] When front: "Tap card to reveal meaning"
  - [ ] When back: "How well did you know this?"

#### Interactions - Front Side
- [ ] Tap card → Flips to back (animation)
- [ ] Back side shows:
  - [ ] Lightbulb icon
  - [ ] "Meaning" label
  - [ ] Definition displayed
  - [ ] Example sentence (if available)
  - [ ] Yellow/orange background

#### Interactions - Back Side
- [ ] "Still Learning" button visible:
  - [ ] Red/close icon
  - [ ] Red border
- [ ] "Know It" button visible:
  - [ ] Green/check icon
  - [ ] Green background

#### Scoring
- [ ] Tap "Still Learning":
  - [ ] Card moves to end of queue
  - [ ] Incorrect count increases
  - [ ] Total cards increases by 1
- [ ] Tap "Know It":
  - [ ] Advances to next card
  - [ ] Correct count increases
  - [ ] Total cards stays same

#### Completion Screen
- [ ] Trophy icon visible (color based on accuracy)
- [ ] "Study Session Complete! 🎉" text shows
- [ ] Accuracy card shows:
  - [ ] "Accuracy" label
  - [ ] Percentage shows
  - [ ] Stats row:
    - [ ] Known count (green)
    - [ ] Learning count (red)
- [ ] "Study Again" button visible
- [ ] "Done" button visible
- [ ] Tap "Study Again" → Starts new session
- [ ] Tap "Done" → Returns to home

---

## 🔄 Navigation Testing

### Navigation Flow
- [ ] Home → Select Book → Lessons List ✓
- [ ] Lessons List → Back → Home ✓
- [ ] Lessons List → Select Lesson → Audio Player ✓
- [ ] Audio Player → Back → Lessons List ✓
- [ ] Lessons List → Menu → Transcription ✓
- [ ] Transcription → Back → Lessons List ✓
- [ ] Lessons List → Menu → Quiz ✓
- [ ] Quiz → Back → Lessons List ✓
- [ ] Home → Flashcards tab → Flashcards ✓
- [ ] Flashcards → Back → Home ✓

### System Back Button
- [ ] Press back from Player → Returns to Lessons List
- [ ] Press back from Transcription → Returns to Lessons List
- [ ] Press back from Quiz → Returns to Lessons List
- [ ] Press back from Flashcards → Returns to Home
- [ ] Press back from Lessons List → Returns to Home
- [ ] Press back from Home → Exits app

---

## 💾 Data Persistence Testing

### Progress Tracking
- [ ] Complete a lesson → Progress saves
- [ ] Close and reopen app → Progress retained
- [x] Complete **Transcription Exercise** - Listen and type mode → Score saved
- [x] Complete **Comprehension Quizzes** - Multiple choice questions → Score saved
- [x] Complete **Vocabulary System** - Flashcard interface → Progress saved
- [x] Complete **Progress Dashboard** - Statistics and achievements → Progress saved
- [ ] Rotate device in Player → State preserved
- [ ] Rotate device in Quiz → State preserved
- [ ] Background app → Return → State preserved
- [ ] Force stop → Reopen → Progress saved

---

## 🎨 UI/UX Testing

### Visual Polish
- [ ] Material 3 design consistent
- [ ] Colors match theme (blue, orange, green)
- [ ] Fonts readable
- [ ] Spacing consistent
- [ ] Icons clear and meaningful
- [ ] Animations smooth

### Accessibility
- [ ] Text contrast sufficient
- [ ] Buttons large enough (44x44dp minimum)
- [ ] Touch targets spaced properly
- [ ] Loading indicators show
- [ ] Error messages clear

---

## 🐛 Bug Reporting Template

### Bug Report
```
Title: [Brief description]

**Steps to Reproduce**:
1.
2.
3.

**Expected Behavior**:
[What should happen]

**Actual Behavior**:
[What actually happens]

**Device**:
- Manufacturer: [e.g., Samsung, Google, Xiaomi]
- Model: [e.g., Pixel 5, Galaxy S21]
- Android Version: [e.g., 13, 14]

**Screenshots**:
[Attach if applicable]

**Logs**:
[Run: adb logcat | grep "englishlearning"]
```

---

## ✅ Sign-Off Criteria

### Ready for Release When:
- [ ] All critical features work
- [ ] No crashes during normal use
- [ ] Progress saves correctly
- [ ] UI is polished and consistent
- [ ] Navigation works smoothly
- [ ] Data loads correctly
- [ ] Performance is acceptable

### Known Limitations (Acceptable for Alpha)
- [x] Audio playback uses absolute paths (Resolved)
- [x] Limited quiz content (Samples provided)
- [x] No vocabulary initially (Populates as you study)
- [x] Progress dashboard implemented
- [ ] No offline mode yet

---

**Testing Date**: _____________
**Tester Name**: _____________
**Device Tested**: _____________
**Overall Status**: ⬜ Ready for Release / ⬜ Needs Work

**Notes**:
_________________________________________________________________________
_________________________________________________________________________
_________________________________________________________________________
