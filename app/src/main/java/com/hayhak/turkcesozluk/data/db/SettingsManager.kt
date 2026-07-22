package com.hayhak.turkcesozluk.data.db

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class DictionaryMode {
    SYNONYMS, VERBS, DEFINITIONS, IDIOMS, ADJECTIVES, ALL
}

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME = "ui_theme"
    private const val KEY_MODE = "dictionary_mode"
    private const val KEY_SHOW_DAILY_WORD = "show_daily_word"
    private const val KEY_HAS_SEEN_MODE_HINT = "has_seen_mode_hint"

    private val _themeState = MutableStateFlow(AppTheme.SYSTEM)
    val themeState = _themeState.asStateFlow()

    private val _modeState = MutableStateFlow(DictionaryMode.SYNONYMS)
    val modeState = _modeState.asStateFlow()

    private val _showDailyWordState = MutableStateFlow(true)
    val showDailyWordState = _showDailyWordState.asStateFlow()

    private val _hasSeenModeHintState = MutableStateFlow(false)
    val hasSeenModeHintState = _hasSeenModeHintState.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)
        _themeState.value = runCatching { AppTheme.valueOf(themeName ?: "") }.getOrDefault(AppTheme.SYSTEM)

        val modeName = prefs.getString(KEY_MODE, DictionaryMode.SYNONYMS.name)
        _modeState.value = runCatching { DictionaryMode.valueOf(modeName ?: "") }.getOrDefault(DictionaryMode.SYNONYMS)

        _showDailyWordState.value = prefs.getBoolean(KEY_SHOW_DAILY_WORD, true)
        _hasSeenModeHintState.value = prefs.getBoolean(KEY_HAS_SEEN_MODE_HINT, false)
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

    fun setShowDailyWord(context: Context, show: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_DAILY_WORD, show).apply()
        _showDailyWordState.value = show
    }

    fun markModeHintSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_SEEN_MODE_HINT, true).apply()
        _hasSeenModeHintState.value = true
    }
}
