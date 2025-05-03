package com.example.docscan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Scanner


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
            }
        }

    }

    fun savePdf(sourceUri: Uri){
        viewModelScope.launch {
            val isSuccess = docScanRepository.savePdf(sourceUri)
            if(!isSuccess){
                _scanState.value = ScanState.Error("Failed to save PDF")
            }
            else{
                getPdfUri()
            }
        }
    }

    fun resetState(){
        _scanState.value = ScanState.Idle
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