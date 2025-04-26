package com.example.docscan


import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
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

