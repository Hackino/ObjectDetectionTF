package com.example.myapplication.extensions

import android.content.Context
import org.tensorflow.lite.support.common.FileUtil


fun Context.loadLabelsFile(filePath:String): MutableList<String> =
    FileUtil.loadLabels(this, filePath)