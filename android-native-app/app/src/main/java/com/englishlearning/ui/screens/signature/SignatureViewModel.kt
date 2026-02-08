package com.englishlearning.ui.screens.signature

import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishlearning.data.repository.SignatureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for signature screen
 */
@HiltViewModel
class SignatureViewModel @Inject constructor(
    private val signatureRepository: SignatureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignatureUiState>(SignatureUiState.Idle)
    val uiState: StateFlow<SignatureUiState> = _uiState.asStateFlow()

    /**
     * Save signature to file
     */
    fun saveSignature(paths: List<Path>, width: Int, height: Int) {
        if (paths.isEmpty()) {
            _uiState.value = SignatureUiState.Error("请先签名")
            return
        }

        viewModelScope.launch {
            _uiState.value = SignatureUiState.Saving

            signatureRepository.saveSignature(
                paths = paths,
                width = width,
                height = height
            )
                .onSuccess { file ->
                    _uiState.value = SignatureUiState.Success(
                        message = "签名已保存: ${file.name}",
                        filePath = file.absolutePath
                    )
                }
                .onFailure { exception ->
                    _uiState.value = SignatureUiState.Error(
                        exception.message ?: "保存失败"
                    )
                }
        }
    }

    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = SignatureUiState.Idle
    }
}

sealed class SignatureUiState {
    object Idle : SignatureUiState()
    object Saving : SignatureUiState()
    data class Success(val message: String, val filePath: String) : SignatureUiState()
    data class Error(val message: String) : SignatureUiState()
}
