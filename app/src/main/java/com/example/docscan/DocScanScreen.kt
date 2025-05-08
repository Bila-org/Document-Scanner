package com.example.docscan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.docscan.ui.theme.DocScanTheme


@Composable
fun DocScanScreen(
    viewModel: ScannerViewModel,
    onScanClicked: () -> Unit,
    onOpenPdf: (Uri) -> Unit,
    onSharePdf: (Uri) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier
) {

    DocScanMain(
        scanState = viewModel.scanState.collectAsState().value,
        onScanClicked = onScanClicked,
        onOpenPdf = onOpenPdf,
        onSharePdf = onSharePdf,
        onBackClick = onBackClick
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScanMain(
    scanState: ScanState,
    onScanClicked: () -> Unit,
    onOpenPdf: (Uri) -> Unit,
    onSharePdf: (Uri) -> Unit,
    onBackClick: () -> Unit,

    ) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = scanState.message, key2 = scanState.imageUris) {
        if (scanState.message.isNotBlank()) {
            snackbarHostState.showSnackbar(scanState.message)
        }
    }

    Scaffold(topBar = {
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

            }, scrollBehavior = scrollBehavior
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                onScanClicked()
            },
        ) {
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Scan a document",
            )
        }
    }, floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
            )
        },

        bottomBar = {
            if (scanState.pdfUri != null) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(onClick = { onOpenPdf(scanState.pdfUri) }) {
                            Text("Open PDF")
                        }
                        OutlinedButton(onClick = { onSharePdf(scanState.pdfUri) }) {
                            Text(
                                text = "Share PDF"
                            )
                        }
                    }
                }
            }
        }

    ) { innerPadding ->

        BackHandler {
            onBackClick()
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ScanItemList(
                itemUris = scanState.imageUris,
                scrollBehavior = scrollBehavior,
                modifier = Modifier.fillMaxSize()
            )

            if (scanState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
private fun DocScanMainPreview() {
    DocScanTheme(darkTheme = true) {
        DocScanMain(scanState = ScanState(
            hasSavedPdf = true,
            isLoading = true,
            pdfUri = Uri.parse(""),
            message = "",
            errorMessage = ""
        ), onScanClicked = {}, onOpenPdf = {}, onSharePdf = {}, onBackClick = {})
    }
}
