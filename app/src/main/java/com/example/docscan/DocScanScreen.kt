package com.example.docscan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope


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
        floatingActionButtonPosition = FabPosition.Center

    ) {innerPadding ->
        BackHandler {
            onBackClick()
        }

        val context = LocalContext.current
        val scanState = viewModel.scanState.collectAsState().value
        val pdfUriState = viewModel.pdfUriState.collectAsState().value

        /*
        lifeCycleOwner = Life
        LaunchedEffect ( scantState, lifeCycleOwner ){
            snapshotFlow(){
                val error as scanState.message
                Toast.makeText(
                    context,
                    "Error ${scanState.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        */


        when(scanState) {
            is ScanState.Error -> {
                LaunchedEffect(scanState) {
                    /* Use snack bar instead of toast
                               * */
                    Toast.makeText(
                        context,
                        "Error ${scanState.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            ScanState.Idle -> {
            }

            ScanState.Loading -> {
                CircularProgressIndicator()
            }

            is ScanState.Success -> {
                LaunchedEffect(scanState.result.pdfUri) {
                    if (scanState.result.pdfUri != null) {
                        /*
                        Toast.makeText(
                            context,
                            "PDF created with ${scanState.result.pageCount} pages",
                            Toast.LENGTH_LONG
                        ).show()
                         */
                        savePdf(scanState.result.pdfUri)


                        //Text("PDF created with ${scanState.result.pageCount} pages")
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
            openPdf(context = context, pdfUri = pdfUriState)
        }
    }
}


private fun openPdf(context: Context, pdfUri: Uri) {
    try {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            //type = "application/pdf"
            setDataAndType(pdfUri, "application/pdf")
           // putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          //  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // required for non-activity contexts
        }
        context.startActivity(Intent.createChooser(openIntent, "Open pdf with..."))
       // context.startActivity(Intent.createChooser(openIntent, "Open PDF"))

    } catch (e: Exception) {
        Toast.makeText(context, "Failed to open PDF: ${e.message}", Toast.LENGTH_LONG).show()
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
