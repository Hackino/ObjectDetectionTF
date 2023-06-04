package com.example.myapplication.utils

import android.content.Context
import android.util.Log
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

fun Context.askForPermissions(permissions:List<String>, action:()->Unit) {
    Dexter.withContext(this).withPermissions(permissions).withListener(object :
        MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) action()
        }
        override fun onPermissionRationaleShouldBeShown(
            permissions: List<PermissionRequest>,
            token: PermissionToken
        ) {
            token.continuePermissionRequest()
        }
    })
        .withErrorListener { error: DexterError? -> }
        .onSameThread()
        .check()
}