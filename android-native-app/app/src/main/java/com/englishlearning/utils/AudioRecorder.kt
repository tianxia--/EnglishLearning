package com.englishlearning.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles audio recording using MediaRecorder
 */
@Singleton
class AudioRecorder @Inject constructor() {

    private var recorder: MediaRecorder? = null
    
    /**
     * Start recording to the specified file
     */
    fun startRecording(context: Context, outputFile: File) {
        // Ensure any previous recording is stopped
        stopRecording()

        createRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            
            try {
                prepare()
                start()
                recorder = this
            } catch (e: IOException) {
                e.printStackTrace()
                reset()
                release()
                recorder = null
            }
        }
    }

    /**
     * Stop recording
     */
    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                // Ignore failure if stop is called too early
            }
            reset()
            release()
        }
        recorder = null
    }

    private fun createRecorder(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
}
