package com.example.myapplication.pref

interface PreferenceCache {
    fun getLanguageCode(): String?
    fun setLanguageCode(languageCode: String)
}