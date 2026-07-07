package com.hayhak.esanlamli.data.db

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class DictionaryMode {
    SYNONYMS, VERBS
}

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME = "ui_theme"
    private const val KEY_MODE = "dictionary_mode"

    private val _themeState = MutableStateFlow(AppTheme.SYSTEM)
    val themeState = _themeState.asStateFlow()

    private val _modeState = MutableStateFlow(DictionaryMode.SYNONYMS)
    val modeState = _modeState.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)
        _themeState.value = AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)

        val modeName = prefs.getString(KEY_MODE, DictionaryMode.SYNONYMS.name)
        _modeState.value = DictionaryMode.valueOf(modeName ?: DictionaryMode.SYNONYMS.name)
    }

    fun setTheme(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _themeState.value = theme
    }

    fun setDictionaryMode(context: Context, mode: DictionaryMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _modeState.value = mode
    }
}
