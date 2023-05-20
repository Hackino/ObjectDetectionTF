package com.example.myapplication.utils.extensions

import android.app.Activity
import android.view.WindowManager

fun Activity.fullScreen() { window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) }


