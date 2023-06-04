package com.example.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        var instance: MyApplication? = null
    }
    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}