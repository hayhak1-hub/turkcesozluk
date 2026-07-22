package com.hayhak.turkcesozluk.data.db

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
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    suspend fun updateStreak(context: Context) {
        mutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val today = sdf.format(Date())

            val currentDayActions = prefs.getInt(KEY_STATS_PREFIX + today, 0)
            prefs.edit().putInt(KEY_STATS_PREFIX + today, currentDayActions + 1).apply()

            val lastDate = prefs.getString(KEY_LAST_ACTIVITY, "")
            val currentStreak = prefs.getInt(KEY_STREAK_COUNT, 0)

            if (lastDate == today) return

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)

            val newStreak = when (lastDate) {
                yesterday -> currentStreak + 1
                else -> 1
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
            val today = sdf.format(Date())

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)

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

            val calendar = Calendar.getInstance()
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            repeat(7) {
                val dateStr = sdf.format(calendar.time)
                stats.add(prefs.getInt(KEY_STATS_PREFIX + dateStr, 0))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return stats
        }
    }

    suspend fun getPreviousWeekTotal(context: Context): Int {
        mutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val calendar = Calendar.getInstance()
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            calendar.add(Calendar.DAY_OF_YEAR, -7)

            var total = 0
            repeat(7) {
                val dateStr = sdf.format(calendar.time)
                total += prefs.getInt(KEY_STATS_PREFIX + dateStr, 0)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return total
        }
    }
}
