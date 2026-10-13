package com.hayhak.turkcesozluk.util

import android.content.Context
import java.util.Calendar

/**
 * "Bizi değerlendir" penceresini sınırlar: varsayılan olarak 3 günde 1; kullanıcı bir kez
 * Google Play'e gittiyse haftada 1. İlk gösterim de kurulumdan en az bir aralık sonra yapılır,
 * böylece kullanıcıya önce uygulamayı denemesi için zaman tanınır.
 *
 * Sınır pencereye aittir, onu tetikleyen ekrana değil: quiz ve oyun aynı kaydı paylaşır.
 */
class RatePromptStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun canShowNow(now: Long = System.currentTimeMillis()): Boolean {
        val reference = prefs.getLong(KEY_LAST_SHOWN, 0L).takeIf { it != 0L }
            ?: firstSeen(now)
        return isDue(reference, now, intervalDays())
    }

    fun markShown(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SHOWN, now).apply()
    }

    /** Kullanıcı Play Store sayfasını açtı; bundan sonra daha seyrek sorulur. */
    fun markVisitedPlay() {
        prefs.edit().putBoolean(KEY_VISITED_PLAY, true).apply()
    }

    private fun intervalDays(): Int =
        if (prefs.getBoolean(KEY_VISITED_PLAY, false)) DAYS_AFTER_PLAY_VISIT else DAYS_DEFAULT

    private fun firstSeen(now: Long): Long {
        val stored = prefs.getLong(KEY_FIRST_SEEN, 0L)
        if (stored != 0L) return stored
        prefs.edit().putLong(KEY_FIRST_SEEN, now).apply()
        return now
    }

    companion object {
        private const val PREFS = "rate_prompt"
        private const val KEY_LAST_SHOWN = "last_shown"
        private const val KEY_FIRST_SEEN = "first_seen"
        private const val KEY_VISITED_PLAY = "visited_play"

        const val DAYS_DEFAULT = 3
        const val DAYS_AFTER_PLAY_VISIT = 7

        /**
         * [reference] gününden [intervalDays] takvim günü geçtiyse true. Gün, milisaniye
         * eklenerek değil takvimle sayılır; yaz/kış saati geçişinde gün 23 ya da 25 saattir.
         * [reference] gelecekteyse saat geri alınmıştır; yıllarca beklememek için süre dolmuş sayılır.
         */
        fun isDue(reference: Long, now: Long, intervalDays: Int): Boolean {
            if (reference > now) return true
            val due = Calendar.getInstance().apply {
                timeInMillis = reference
                add(Calendar.DAY_OF_YEAR, intervalDays)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return now >= due.timeInMillis
        }
    }
}
