package com.example.myapplication.extensions

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager

fun Context.getCameraManager() = getSystemService(Context.CAMERA_SERVICE) as CameraManager