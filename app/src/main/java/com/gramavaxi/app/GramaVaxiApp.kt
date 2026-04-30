package com.gramavaxi.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gramavaxi.app.notify.NotificationHelper

class GramaVaxiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.ensureChannel(this)
        applySavedLocale()
    }

    private fun applySavedLocale() {
        val lang = prefs().getString(PREF_LANG, null)
        if (!lang.isNullOrBlank()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }
    }

    fun prefs(): SharedPreferences =
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    companion object {
        const val PREFS = "gramavaxi_prefs"
        const val PREF_LANG = "lang"
        lateinit var instance: GramaVaxiApp
            private set
    }
}
