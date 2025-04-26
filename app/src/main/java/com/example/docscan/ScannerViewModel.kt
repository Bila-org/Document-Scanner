package com.example.docscan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ScannerViewModel : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState : StateFlow<ScanState> = _scanState.asStateFlow()

    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(5)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()


    val scanner = GmsDocumentScanning.getClient(options)


    fun handleScanResult(data: Intent?){
        _scanState.value = ScanState.Loading
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

    fun resetState(){
        _scanState.value = ScanState.Idle
    }

}