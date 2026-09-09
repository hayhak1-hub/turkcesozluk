package com.hayhak.turkcesozluk.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "app_language"

    fun applyAppLocale(languageTag: String?) {
        val locales = if (languageTag.isNullOrEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun wrap(context: Context, languageTag: String?): Context {
        if (languageTag.isNullOrEmpty()) return context

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun savedTag(context: Context): String? {
        val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "")
        return tag?.takeIf { it.isNotEmpty() }
    }
}
