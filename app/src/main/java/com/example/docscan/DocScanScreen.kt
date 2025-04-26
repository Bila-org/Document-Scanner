package com.example.docscan

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


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


        when(scanState) {
            is ScanState.Error -> {
                LaunchedEffect(scanState) {
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
                        Toast.makeText(
                            context,
                            "PDF created with ${scanState.result.pageCount} pages",
                            Toast.LENGTH_LONG
                        ).show()
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanItemList(
    scrollBehavior: TopAppBarScrollBehavior,
    itemUris: List<Uri?>,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
){
    LazyColumn(
        modifier = modifier
            .background(androidx.compose.ui.graphics.Color.Black)
            .padding(innerPadding)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        //  contentPadding = PaddingValues(vertical = 32.dp)
    ) {
        items(itemUris.filterNotNull()) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Scanned Document",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}




@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun PreviewDocScanScreen(
){
     DocScanScreen(
         viewModel = ScannerViewModel(),
         onScanClicked = {},
         savePdf = {},
         modifier = Modifier
             .fillMaxSize()

     )
}