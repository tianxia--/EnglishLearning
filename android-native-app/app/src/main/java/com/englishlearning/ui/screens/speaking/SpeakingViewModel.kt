package com.englishlearning.ui.screens.speaking

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.model.Lesson
import com.englishlearning.data.model.SpeakingRecord
import com.englishlearning.data.repository.ContentRepository
import com.englishlearning.data.repository.SpeakingRepository
import com.englishlearning.utils.AudioRecorder
import com.englishlearning.utils.ScoringUtils
import com.englishlearning.utils.SpeechRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SpeakingViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val speakingRepository: SpeakingRepository,
    private val audioRecorder: AudioRecorder,
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val scoringUtils: ScoringUtils,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>("bookId") ?: ""
    private val lessonId: String = savedStateHandle.get<String>("lessonId") ?: ""

    private val _uiState = MutableStateFlow<SpeakingUiState>(SpeakingUiState.Loading)
    val uiState: StateFlow<SpeakingUiState> = _uiState.asStateFlow()

    private var currentLesson: Lesson? = null
    private var currentSegmentIndex = 0

    init {
        loadLesson()
    }

    fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = SpeakingUiState.Loading
            contentRepository.loadLesson(bookId, lessonId)
                .onSuccess { lesson ->
                    if (lesson != null) {
                        currentLesson = lesson
                        if (lesson.segments.isNotEmpty()) {
                            _uiState.value = SpeakingUiState.Ready(
                                lesson = lesson,
                                currentSegmentIndex = currentSegmentIndex,
                                targetText = lesson.segments[currentSegmentIndex].text
                            )
                        } else {
                            _uiState.value = SpeakingUiState.Error("No segments found in this lesson")
                        }
                    } else {
                        _uiState.value = SpeakingUiState.Error("Lesson not found")
                    }
                }
                .onFailure {
                    _uiState.value = SpeakingUiState.Error(it.message ?: "Failed to load lesson")
                }
        }
    }

    fun startRecording(context: Context) {
        val lesson = currentLesson ?: return
        val state = _uiState.value as? SpeakingUiState.Ready ?: return

        // output file
        val outputDir = context.getExternalFilesDir("speaking_records")
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputFile = File(outputDir, "${lesson.id}_${currentSegmentIndex}_${System.currentTimeMillis()}.m4a")

        // Update state to Recording
        _uiState.value = SpeakingUiState.Recording(
            lesson = lesson,
            currentSegmentIndex = currentSegmentIndex,
            targetText = state.targetText
        )

        try {
            // Start Audio Recorder (files)
            audioRecorder.startRecording(context, outputFile)

            // Start ASR
            viewModelScope.launch {
                speechRecognizerManager.startListening(context)
                    .catch { e ->
                        // Handle ASR error (e.g. no internet, no permission)
                        stopRecordingAndProcess(outputFile, "", error = e.message)
                    }
                    .collect { spokenText ->
                        // Got result
                        stopRecordingAndProcess(outputFile, spokenText)
                    }
            }
        } catch (e: Exception) {
             _uiState.value = SpeakingUiState.Error("Failed to start recording: ${e.message}")
        }
    }

    fun stopRecording() {
         // This is usually called by user action if they want to stop early
         // But logic mainly driven by ASR result or silence
         // We can force stop
         audioRecorder.stopRecording()
    }

    private fun stopRecordingAndProcess(audioFile: File, spokenText: String, error: String? = null) {
        audioRecorder.stopRecording()

        val lesson = currentLesson ?: return
        val currentState = _uiState.value 

        if (currentState is SpeakingUiState.Recording) {
             if (error != null) {
                 _uiState.value = SpeakingUiState.Error(error)
                 return
             }

             // Calculate Score
             val targetText = currentState.targetText
             val accuracy = scoringUtils.calculateSimilarity(targetText, spokenText)
             
             // Create Record
             val record = SpeakingRecord(
                 id = java.util.UUID.randomUUID().toString(),
                 lessonId = lesson.id,
                 bookId = bookId,
                 segmentIndex = currentSegmentIndex,
                 text = spokenText,
                 audioPath = audioFile.absolutePath,
                 accuracyScore = accuracy,
                 fluencyScore = 80f, // Mock fluency for now
                 timestamp = System.currentTimeMillis()
             )

             // Save
             viewModelScope.launch {
                 speakingRepository.saveSpeakingRecord(record)
             }

             // Show Result
             _uiState.value = SpeakingUiState.Result(
                 lesson = lesson,
                 currentSegmentIndex = currentSegmentIndex,
                 targetText = targetText,
                 spokenText = spokenText,
                 score = accuracy,
                 audioPath = audioFile.absolutePath
             )
        }
    }
    
    fun nextSegment() {
        val lesson = currentLesson ?: return
        if (currentSegmentIndex < lesson.segments.size - 1) {
            currentSegmentIndex++
            _uiState.value = SpeakingUiState.Ready(
                lesson = lesson,
                currentSegmentIndex = currentSegmentIndex,
                targetText = lesson.segments[currentSegmentIndex].text
            )
        } else {
             // End of lesson logic?
             // Loop back or show finish?
             // For simple MVP, stay on last or loop
             currentSegmentIndex = 0 
             _uiState.value = SpeakingUiState.Ready(
                lesson = lesson,
                currentSegmentIndex = currentSegmentIndex,
                targetText = lesson.segments[currentSegmentIndex].text
            )
        }
    }
    
    fun retry() {
        val lesson = currentLesson ?: return
        _uiState.value = SpeakingUiState.Ready(
            lesson = lesson,
            currentSegmentIndex = currentSegmentIndex,
            targetText = lesson.segments[currentSegmentIndex].text
        )
    }
}

sealed class SpeakingUiState {
    object Loading : SpeakingUiState()
    data class Error(val message: String) : SpeakingUiState()
    
    data class Ready(
        val lesson: Lesson,
        val currentSegmentIndex: Int,
        val targetText: String
    ) : SpeakingUiState()

    data class Recording(
        val lesson: Lesson,
        val currentSegmentIndex: Int,
        val targetText: String
    ) : SpeakingUiState()

    data class Result(
        val lesson: Lesson,
        val currentSegmentIndex: Int,
        val targetText: String,
        val spokenText: String,
        val score: Float,
        val audioPath: String
    ) : SpeakingUiState()
}
