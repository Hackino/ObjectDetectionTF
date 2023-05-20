package com.example.myapplication.utils.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

const val PERMISSION_CAMERA = Manifest.permission.CAMERA

fun Context.isCameraPermissionGranted()=
    checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED

