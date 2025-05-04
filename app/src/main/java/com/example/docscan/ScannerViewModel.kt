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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class DocScanResult(
    val imageUris: List<Uri?>,
    val pdfUri: Uri?,
    val pageCount: Int?
)

sealed interface ScanState {
    object Idle : ScanState
    object Loading : ScanState
    data class Success(val result: DocScanResult) : ScanState
    data class Error(val message: String) : ScanState
}

class ScannerViewModel(
    private val docScanRepository: DocScanRepository
) : ViewModel() {

    data class ScreenState(
        val hasSavedPdf: Boolean = false,
        val isLoading: Boolean = false,
        val pdfUri: Uri? = null,
        val errorMessage: String = "",
        val message: String = ""
    )

    val screenState = MutableStateFlow<ScreenState>(ScreenState())

    fun error(message: String) {
        screenState.update {
            it.copy(
                errorMessage = message
            )
        }
    }

    fun message(message: String) {
        screenState.update {
            it.copy(
                message = message
            )
        }
    }

    fun resetScreenState() {
        screenState.update {
            it.copy(
                hasSavedPdf = false,
                isLoading = false,
                pdfUri = null,
                errorMessage = "",
                message = ""
            )
        }
    }

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _pdfUriState = MutableStateFlow<Uri?>(null)
    val pdfUriState: StateFlow<Uri?> = _pdfUriState.asStateFlow()


    private val _snackbarEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarEvent.emit(message)
        }
    }


    fun handleScanResult(data: Intent?) = viewModelScope.launch {
        try {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
            val pages = scanResult?.pages?.mapNotNull {
                it.imageUri
            }
            val pdf = scanResult?.pdf
            val uri = pdf?.let {
                savePdf(it.uri)
            }

            screenState.update {
                it.copy(
                    hasSavedPdf = uri != null,
                    isLoading = false,
                    pdfUri = uri,
                    errorMessage = "",
                    message = ""
                )
            }

        } catch (e: Exception) {
            screenState.update {
                it.copy(
                    hasSavedPdf = false,
                    isLoading = false,
                    pdfUri = null,
                    errorMessage = e.message ?: "Error scanning document",
                    message = ""
                )
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