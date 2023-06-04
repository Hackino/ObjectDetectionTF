package com.example.myapplication.pref

import android.content.Context
import com.example.myapplication.MyApplication
import java.util.Locale

object PreferenceCacheImpl : PreferenceCache {

    private val sharedPreferences = MyApplication.instance?.getSharedPreferences(
        "pref_test",
        Context.MODE_PRIVATE
    )

    override fun setLanguageCode(languageCode: String) {
        sharedPreferences?.edit()
            ?.putString(PreferenceConstants.LANGUAGE_CODE_KEY, languageCode)?.apply()
    }

    override fun getLanguageCode(): String? =
        sharedPreferences?.getString(
            PreferenceConstants.LANGUAGE_CODE_KEY,
            Locale.getDefault().language
        )

}