package com.example.docscan


import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.docscan.ui.theme.DocScanTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            viewModel.handleScanResult(it.data)
        }
    }
    private val viewModel: ScannerViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                val context = LocalContext.current
                val activity = context as Activity

                DocScanScreen(
                    viewModel = viewModel,
                    onScanClicked = {
                        viewModel.scanner.getStartScanIntent(activity)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    applicationContext,
                                    it.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                    savePdf = { uri->
                        try{
                            val fos = FileOutputStream(File(filesDir, "Scan.pdf"))
                            contentResolver.openInputStream(uri)?.use{
                                it.copyTo(fos)
                            }
                        } catch (e: Exception){
                            Toast.makeText(context,
                                "Failed to save pdf", Toast.LENGTH_LONG).show()
                        }
                    },
                    onBackClick = {
                        finish()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )

            }
        }
    }

}

