package com.example.myapplication.utils.extensions

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager

fun Activity.getCameraManager() = getSystemService(Context.CAMERA_SERVICE) as CameraManager