package com.englishlearning.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.asAndroidPath

/**
 * Repository for managing signature storage
 */
@Singleton
class SignatureRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val signaturesDir: File
        get() = File(context.filesDir, "signatures").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Get all saved signatures
     */
    fun getSavedSignatures(): List<File> {
        return signaturesDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /**
     * Delete a signature file
     */
    fun deleteSignature(file: File): Boolean {
        return file.delete()
    }

    /**
     * Save signature paths as an image file
     */
    suspend fun saveSignature(
        paths: List<ComposePath>,
        width: Int,
        height: Int,
        strokeWidth: Float = 6f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Create bitmap with white background
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill background with white
            canvas.drawColor(Color.WHITE)

            // Create paint for the signature
            val paint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

            // Draw each path
            paths.forEach { composePath ->
                try {
                    val androidPath = composePath.asAndroidPath()
                    canvas.drawPath(androidPath, paint)
                } catch (e: Exception) {
                    // Skip invalid paths
                }
            }

            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "signature_$timestamp.png"
            val file = File(signaturesDir, filename)

            // Save bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Recycle bitmap
            bitmap.recycle()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
