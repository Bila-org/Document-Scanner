package com.example.docscan

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ScanState(
    val imageUris: List<Uri?> = emptyList(),
    val hasSavedPdf: Boolean = false,
    val isLoading: Boolean = false,
    val pdfUri: Uri? = null,
    val message: String = "",
    val errorMessage: String = ""
)

class ScannerViewModel(
    private val docScanRepository: DocScanRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()


    fun message(message: String) {
        _scanState.update {
            it.copy(
                message = message
            )
        }
    }

    fun errorMessage(errorMessage: String) {
        _scanState.update {
            it.copy(
                errorMessage = errorMessage
            )
        }
    }

    fun resetScanState() {
        _scanState.update {
            it.copy(
                imageUris = emptyList(),
                hasSavedPdf = false,
                isLoading = false,
                pdfUri = null,
                message = "",
                errorMessage = ""
            )
        }
    }

    fun handleScanResult(data: Intent?) {
        //      _scanState.value = ScanState.Loading
        viewModelScope.launch {
            try {
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
                val pages = scanResult?.pages?.mapNotNull {
                    it.imageUri
                }
                val pdf = scanResult?.pdf
                val uri = pdf?.let {
                    savePdf(it.uri)
                }

                _scanState.update {
                    it.copy(
                        imageUris = pages ?: emptyList(),
                        hasSavedPdf = uri != null,
                        isLoading = false,
                        pdfUri = uri,
                        message = (if(uri != null) {
                            "PDf created successfully"
                        } else {
                            ""
                        }).toString(),
                        errorMessage = ""
                    )
                }

            } catch (e: Exception) {
                _scanState.update {
                    it.copy(
                        hasSavedPdf = false,
                        isLoading = false,
                        pdfUri = null,
                        message = "",
                        errorMessage = e.message ?: "Error scanning document"
                    )
                }
            }
        }
    }

    suspend fun savePdf(sourceUri: Uri): Uri? {
        return docScanRepository.savePdf(sourceUri)
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as DocScanApplication)
                val docScanRepository = application.docScanRepository
                ScannerViewModel(docScanRepository = docScanRepository)
            }
        }
    }
}