package com.hayhak.esanlamli.data.db

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*

object UserStatsManager {
    private const val PREFS_NAME = "user_stats_prefs"
    private const val KEY_LAST_ACTIVITY = "last_activity_date"
    private const val KEY_STREAK_COUNT = "streak_count"
    private const val KEY_STATS_PREFIX = "daily_stat_"
    
    private val mutex = Mutex()

    suspend fun updateStreak(context: Context) {
        mutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Günlük aktivite sayacını artır
            val currentDayActions = prefs.getInt(KEY_STATS_PREFIX + today, 0)
            prefs.edit().putInt(KEY_STATS_PREFIX + today, currentDayActions + 1).apply()

            val lastDate = prefs.getString(KEY_LAST_ACTIVITY, "")
            val currentStreak = prefs.getInt(KEY_STREAK_COUNT, 0)
            
            if (lastDate == today) return

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val newStreak = when (lastDate) {
                yesterday -> currentStreak + 1 // Seri devam ediyor
                else -> 1 // Seri bozuldu veya yeni başladı
            }

            prefs.edit()
                .putString(KEY_LAST_ACTIVITY, today)
                .putInt(KEY_STREAK_COUNT, newStreak)
                .apply()
        }
    }

    suspend fun getStreak(context: Context): Int {
        mutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastDate = prefs.getString(KEY_LAST_ACTIVITY, "")
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            return if (lastDate == today || lastDate == yesterday) {
                prefs.getInt(KEY_STREAK_COUNT, 0)
            } else {
                0
            }
        }
    }

    suspend fun getWeeklyStats(context: Context): List<Int> {
        mutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val stats = mutableListOf<Int>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            val calendar = Calendar.getInstance()
            // Pazartesi gününe kadar geri git
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            
            // Pazartesi'den Pazar'a kadar verileri çek
            for (i in 0..6) {
                val dateStr = sdf.format(calendar.time)
                stats.add(prefs.getInt(KEY_STATS_PREFIX + dateStr, 0))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return stats
        }
    }
}
