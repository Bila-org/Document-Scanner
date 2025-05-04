package com.example.docscan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.docscan.ui.theme.DocScanTheme
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
private fun DocScanMainPreview() {
    DocScanTheme(darkTheme = true) {
        DocScanMain(
            screenState = ScannerViewModel.ScreenState(
                hasSavedPdf = true,
                isLoading = true,
                pdfUri = Uri.parse(""),
            ),
            onStartScan = {},
            onOpenPdf = {},
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScanScreen(
    viewModel: ScannerViewModel,
    onScanClicked: () -> Unit,
    onBackClick: () -> Unit,
    onOpenPdf: (Uri) -> Unit
) {
    //api call response
    //view interaction
    //

    DocScanMain(
        screenState = viewModel.screenState.collectAsState().value,
        onStartScan = onScanClicked,
        onBackClick = onBackClick,
        onOpenPdf = onOpenPdf
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScanMain(
    screenState: ScannerViewModel.ScreenState,
    onStartScan: () -> Unit,
    onOpenPdf: (Uri) -> Unit,
    onBackClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Document Scan App"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Doc Scan"
                        )
                    }

                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartScan,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Scan a document",
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }

    ) { innerPadding ->
        BackHandler {
            onBackClick()
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(

            ) {
                if (screenState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (screenState.pdfUri != null) {
                    Button(
                        onClick = {
                            onOpenPdf(screenState.pdfUri)
                        }) {
                        Text(text = "Open PDF")
                    }
                    Button(onClick = {

                    }) {
                        Text(text = "Share PDF")
                    }
                }
            }
        }

    }
}


private fun sharePdf(context: Context, pdfUri: Uri) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
