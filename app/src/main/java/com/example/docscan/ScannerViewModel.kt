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
import kotlinx.coroutines.launch


data class DocScanResult(
    val imageUris: List<Uri?>,
    val pdfUri: Uri?,
    val pageCount: Int?
)

sealed interface ScanState{
    object Idle : ScanState
    object Loading : ScanState
    data class Success(val result: DocScanResult) : ScanState
    data class Error(val message: String): ScanState
}

class ScannerViewModel(
    private val docScanRepository: DocScanRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState : StateFlow<ScanState> = _scanState.asStateFlow()

    private val _pdfUriState = MutableStateFlow<Uri?>(null)
    val pdfUriState :StateFlow<Uri?> = _pdfUriState.asStateFlow()


    private val _snackbarEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun showSnackbar(message: String){
        viewModelScope.launch {
            _snackbarEvent.emit(message)
        }
    }


    fun handleScanResult(data: Intent?){
  //      _scanState.value = ScanState.Loading
        viewModelScope.launch{
            try{
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
                val pages = scanResult?.pages?.mapNotNull {
                    it.imageUri
                }
                val pdf = scanResult?.pdf

                _scanState.value = ScanState.Success(
                    DocScanResult(
                        imageUris = pages?: emptyList(),
                        pdfUri = pdf?.uri,
                        pageCount = pdf?.pageCount
                    )
                )
            }catch (e:Exception){
                _scanState.value = ScanState.Error(
                    e.message?: "Unknown error occurred during scanning"
                )
                showSnackbar("Scan failed")
            }
        }

    }

    fun savePdf(sourceUri: Uri){
        viewModelScope.launch {
            val isSuccess = docScanRepository.savePdf(sourceUri)
            if(!isSuccess){
                _scanState.value = ScanState.Error("Failed to save PDF")
                showSnackbar("Failed to save PDF")
            }
            else{
                getPdfUri()
            }
        }
    }

    fun resetState(){
        _scanState.value = ScanState.Idle
        _pdfUriState.value = null
    }

    fun resetPdfUriState(){
        _pdfUriState.value = null
    }
    private fun getPdfUri(){
        val pdfUri = docScanRepository.getPdfUri()
        if (pdfUri != null) {
            _pdfUriState.value = pdfUri
        }
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