package com.englishlearning.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

/**
 * State holder for signature pad
 */
class SignatureState {
    private val _paths = mutableStateOf(listOf<Path>())
    private var _currentPath by mutableStateOf<Path?>(null)
    private val _version = mutableStateOf(0)

    val paths: List<Path> get() = _paths.value
    val currentPath: Path? get() = _currentPath
    val version: Int get() = _version.value
    val hasSignature: Boolean get() = _paths.value.isNotEmpty() || _currentPath != null

    fun startPath(offset: Offset) {
        _currentPath = Path().apply { moveTo(offset.x, offset.y) }
        _version.value++
    }

    fun addToPath(offset: Offset) {
        // Create a new Path to trigger state observation
        _currentPath?.let { existingPath ->
            val newPath = Path().apply {
                // Copy existing path
                addPath(existingPath)
                // Add new point
                lineTo(offset.x, offset.y)
            }
            _currentPath = newPath
        }
        _version.value++
    }

    fun endPath() {
        _currentPath?.let {
            _paths.value = _paths.value + it
        }
        _currentPath = null
        _version.value++
    }

    fun clear() {
        _paths.value = emptyList()
        _currentPath = null
        _version.value++
    }

    fun getAllPaths(): List<Path> = _paths.value + listOfNotNull(_currentPath)
}

@Composable
fun rememberSignatureState() = remember { SignatureState() }

/**
 * Signature pad component for hand-written signatures
 *
 * @param modifier Modifier for the canvas
 * @param signatureState State holder for the signature
 * @param penColor Color of the signature stroke
 * @param strokeWidth Width of the signature stroke
 * @param backgroundColor Background color of the canvas
 */
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    signatureState: SignatureState = rememberSignatureState(),
    penColor: Color = Color.Black,
    strokeWidth: Float = 6f,
    backgroundColor: Color = Color.White
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        signatureState.startPath(offset)
                    },
                    onDrag = { change, _ ->
                        signatureState.addToPath(change.position)
                        change.consume()
                    },
                    onDragEnd = {
                        signatureState.endPath()
                    },
                    onDragCancel = {
                        signatureState.endPath()
                    }
                )
            }
    ) {
        // Read version to force redraw on every change
        val version = signatureState.version
        val paths = signatureState.paths
        val currentPath = signatureState.currentPath

        // Draw all completed paths
        paths.forEach { path ->
            drawPath(
                path = path,
                color = penColor,
                style = Stroke(width = strokeWidth)
            )
        }

        // Draw current path being drawn
        currentPath?.let { path ->
            drawPath(
                path = path,
                color = penColor,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
