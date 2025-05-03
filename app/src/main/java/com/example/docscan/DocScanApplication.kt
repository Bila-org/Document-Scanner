package com.example.docscan

import android.app.Application

class DocScanApplication : Application() {

    val docScanRepository : DocScanRepository by lazy {
        DocScanRepository(applicationContext)
    }
}