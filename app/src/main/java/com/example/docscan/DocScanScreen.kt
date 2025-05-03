package com.example.docscan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScanScreen(
    viewModel: ScannerViewModel,
    onScanClicked: () -> Unit,
    savePdf: (Uri) -> Unit,
    onBackClick: ()-> Unit = {},
    modifier: Modifier
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember {SnackbarHostState()}


    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect {message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
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
                onClick = {
                    onScanClicked()
                },
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

    ) {innerPadding ->
        BackHandler {
            onBackClick()
        }

        val context = LocalContext.current
        val scanState = viewModel.scanState.collectAsState().value
        val pdfUriState = viewModel.pdfUriState.collectAsState().value

        when(scanState) {
            is ScanState.Error -> {}
            ScanState.Idle -> {}

            ScanState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is ScanState.Success -> {
                LaunchedEffect(scanState.result.pdfUri) {
                    if (scanState.result.pdfUri != null) {
                        viewModel.showSnackbar("PDF created with ${scanState.result.pageCount} pages")
                        savePdf(scanState.result.pdfUri)
                    }
                }
                ScanItemList(
                    itemUris = scanState.result.imageUris,
                    scrollBehavior = scrollBehavior,
                    innerPadding = innerPadding
                )
            }
        }

        if (pdfUriState != null) {
            //sharePdf(context = context, pdfUri = pdfUriState)
            openPdf(context = context, pdfUri = pdfUriState, viewModel = viewModel)
        }

    }
}


private fun openPdf(context: Context, pdfUri: Uri, viewModel: ScannerViewModel) {
    try {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // required for non-activity contexts
        }
        context.startActivity(Intent.createChooser(openIntent, "Open pdf with..."))
        viewModel.resetPdfUriState()
        // context.startActivity(Intent.createChooser(openIntent, "Open PDF"))

    } catch (e: Exception) {
        viewModel.showSnackbar("Failed to open PDF: ${e.message}")
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
