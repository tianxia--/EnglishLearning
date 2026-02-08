package com.englishlearning.ui.screens.signature

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.englishlearning.ui.components.SignaturePad
import com.englishlearning.ui.components.rememberSignatureState
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Signature screen for creating hand-written signatures
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SignatureViewModel = hiltViewModel()
) {
    val signatureState = rememberSignatureState()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手写签名") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instructions
            Text(
                text = "请在下方区域签名",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Signature pad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .onGloballyPositioned { coordinates ->
                        canvasSize = coordinates.size
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {
                SignaturePad(
                    signatureState = signatureState,
                    penColor = Color.Black,
                    strokeWidth = with(LocalDensity.current) { 4.dp.toPx() },
                    backgroundColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clear button
                OutlinedButton(
                    onClick = { signatureState.clear() },
                    modifier = Modifier.weight(1f),
                    enabled = signatureState.hasSignature
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清除")
                }

                // Save button
                Button(
                    onClick = {
                        viewModel.saveSignature(
                            paths = signatureState.getAllPaths(),
                            width = canvasSize.width,
                            height = canvasSize.height
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = signatureState.hasSignature && uiState !is SignatureUiState.Saving
                ) {
                    when (val state = uiState) {
                        is SignatureUiState.Saving -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存中...")
                        }
                        else -> {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = "提示：签名将保存为 PNG 图片，存储在应用目录中",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Result dialog
        when (val state = uiState) {
            is SignatureUiState.Success -> {
                ResultDialog(
                    message = state.message,
                    onDismiss = {
                        viewModel.resetState()
                        signatureState.clear()
                    }
                )
            }
            is SignatureUiState.Error -> {
                ResultDialog(
                    message = state.message,
                    isError = true,
                    onDismiss = { viewModel.resetState() }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ResultDialog(
    message: String,
    isError: Boolean = false,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isError) "失败" else "成功",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确定")
                }
            }
        }
    }
}
