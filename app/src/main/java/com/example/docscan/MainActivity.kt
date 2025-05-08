package com.example.docscan


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.docscan.ui.theme.DocScanTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

class MainActivity : ComponentActivity() {

    val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            viewModel.handleScanResult(it.data)
        }
    }

    private val viewModel: ScannerViewModel by viewModels { ScannerViewModel.Factory }

    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(5)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()

    private val scanner = GmsDocumentScanning.getClient(options)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                //  val snackbarHostState = remember { SnackbarHostState() }
                //  val scope = rememberCoroutineScope()
                //    Box(
                //     modifier = Modifier.fillMaxSize()
                //   ) {
                /*
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )*/

                DocScanScreen(
                    viewModel = viewModel,
                    onScanClicked = {
                        scanner.getStartScanIntent(this@MainActivity)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(
                                    IntentSenderRequest.Builder(intentSender).build()
                                )
                            }
                            .addOnFailureListener {
                                it.message?.let { it1 -> viewModel.message(it1) }
                                /* scope.launch {
                                     it.message?.let { it1 -> snackbarHostState.showSnackbar(it1) }
                                 }*/
                            }
                    },
                    onOpenPdf = {
                        try {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(it, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                //  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // required for non-activity contexts
                            }
                            this@MainActivity.startActivity(
                                Intent.createChooser(
                                    openIntent,
                                    "Open pdf with..."
                                )
                            )

                        } catch (e: Exception) {
                            viewModel.message("Failed to open PDF: ${e.message}")
                            /*scope.launch {
                                snackbarHostState.showSnackbar("Failed to open PDF: ${e.message}")
                            }*/
                        }
                    },
                    onSharePdf = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, it)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            this@MainActivity.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share PDF"
                                )
                            )
                        } catch (e: Exception) {
                            viewModel.message("Failed to share PDF: ${e.message}")
                            /* scope.launch {
                                 snackbarHostState.showSnackbar("Failed to share PDF: ${e.message}")
                             }*/
                        }
                    },
                    onBackClick = { finish() },
                    modifier = Modifier.fillMaxSize()
                )
                //  }
            }
        }
    }
}
