package com.englishlearning.ui.screens.player

import com.englishlearning.data.model.Segment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages LRC transcript synchronization with audio playback
 */
@Singleton
class LrcSyncManager @Inject constructor() {

    private var segments: List<Segment> = emptyList()

    private val _currentSegmentIndex = MutableStateFlow(0)
    val currentSegmentIndex: StateFlow<Int> = _currentSegmentIndex.asStateFlow()

    private val _isSyncEnabled = MutableStateFlow(true)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    /**
     * Load segments for a lesson
     */
    fun loadSegments(segmentList: List<Segment>) {
        segments = segmentList
        _currentSegmentIndex.value = 0
        android.util.Log.d("LrcSyncManager", "Loaded ${segments.size} segments")
        segments.take(5).forEach {
            android.util.Log.d("LrcSyncManager", "  [${it.startTime}s] ${it.text.take(30)}")
        }
    }

    /**
     * Update current segment based on playback position
     * Uses forward-only progression with hysteresis to prevent oscillation
     */
    fun updatePosition(positionSeconds: Double) {
        if (!_isSyncEnabled.value || segments.isEmpty()) {
            _currentSegmentIndex.value = -1
            return
        }

        val currentIndex = _currentSegmentIndex.value
        val hysteresis = 0.3 // Hysteresis buffer in seconds - prevents rapid switching

        // Find the appropriate segment using forward-only progression
        var newSegmentIndex = currentIndex

        // Find the last segment whose startTime <= positionSeconds
        // This ensures we always pick the segment we've most recently entered
        var candidateIndex = -1
        for (i in segments.indices) {
            if (segments[i].startTime <= positionSeconds) {
                candidateIndex = i
            } else {
                break // Found a segment that starts in the future, stop here
            }
        }

        // If no segment found (position is before first segment), use first segment
        if (candidateIndex == -1) {
            candidateIndex = 0
        }

        // Apply hysteresis to prevent oscillation at boundaries
        // Only move forward to next segment if we're clearly past current segment's start + hysteresis
        // Only move backward if we're clearly before current segment's start - hysteresis
        when {
            candidateIndex > currentIndex -> {
                // Moving forward: check if we're far enough past current segment
                val currentSegment = segments[currentIndex]
                if (positionSeconds >= currentSegment.startTime + hysteresis) {
                    newSegmentIndex = candidateIndex
                }
            }
            candidateIndex < currentIndex -> {
                // Moving backward: only allow if we seeked back significantly
                val targetSegment = segments[candidateIndex]
                val currentSegment = segments[currentIndex]
                // Only move back if position is clearly before current segment
                if (positionSeconds < currentSegment.startTime - hysteresis) {
                    newSegmentIndex = candidateIndex
                }
            }
            // candidateIndex == currentIndex: no change needed
        }

        // Ensure index is within valid range
        newSegmentIndex = newSegmentIndex.coerceIn(0, segments.size - 1)

        // Only log and update if segment actually changed
        if (newSegmentIndex != currentIndex) {
            val segment = segments[newSegmentIndex]
            android.util.Log.d(
                "LrcSyncManager",
                "[${String.format("%.2f", positionSeconds)}s] Seg $currentIndex -> $newSegmentIndex"
            )
            android.util.Log.d(
                "LrcSyncManager",
                "  [${segment.startTime}s-${if (segment.endTime > 0) segment.endTime else "?"}s] ${segment.text.take(40)}"
            )
            _currentSegmentIndex.value = newSegmentIndex
        }
    }

    /**
     * Get current segment
     */
    fun getCurrentSegment(): Segment? {
        return segments.getOrNull(_currentSegmentIndex.value)
    }

    /**
     * Get segment at specific index
     */
    fun getSegmentAt(index: Int): Segment? {
        return segments.getOrNull(index)
    }

    /**
     * Get all segments
     */
    fun getAllSegments(): List<Segment> {
        return segments
    }

    /**
     * Get segment count
     */
    fun getSegmentCount(): Int {
        return segments.size
    }

    /**
     * Jump to specific segment
     */
    fun jumpToSegment(index: Int): Double? {
        val segment = segments.getOrNull(index) ?: return null
        _currentSegmentIndex.value = index
        return segment.startTime
    }

    /**
     * Toggle sync enabled state
     */
    fun toggleSync() {
        _isSyncEnabled.value = !_isSyncEnabled.value
    }

    /**
     * Reset sync manager
     */
    fun reset() {
        segments = emptyList()
        _currentSegmentIndex.value = 0
        _isSyncEnabled.value = true
    }

    /**
     * Check if current segment is highlighted
     */
    fun isSegmentHighlighted(index: Int): Boolean {
        return index == _currentSegmentIndex.value && _isSyncEnabled.value
    }
}
