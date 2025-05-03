package com.example.docscan

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DocScanRepository(private val context: Context) {

    suspend fun savePdf( sourceUri: Uri ): Boolean{
        return withContext(Dispatchers.IO){
            try{
                /*
                Using *.use properly closes the input and output streams
                 */
                FileOutputStream(File(context.filesDir, "Scan.pdf")).use{ fos ->
                    context.contentResolver.openInputStream(sourceUri)?.use{ fis ->
                        fis.copyTo(fos)
                    }
                }
                true
            }catch (e: Exception){
                e.printStackTrace() // For debugging
                false // Failure
            }
        }
    }


    fun hasSavedPdf(): Boolean{
        return getPdfFile().exists()
    }
    fun getPdfFile(): File{
        return File(context.filesDir, "Scan.pdf")
    }

    fun getPdfUri():Uri? {
        val file = getPdfFile()
        return if(file.exists()){
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            null
        }
    }
}