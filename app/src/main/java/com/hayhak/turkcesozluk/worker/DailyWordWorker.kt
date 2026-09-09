package com.hayhak.turkcesozluk.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hayhak.turkcesozluk.MainActivity
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.repository.CoreRepository
import com.hayhak.turkcesozluk.data.repository.DictionaryRepository
import com.hayhak.turkcesozluk.util.LocaleHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker

@HiltWorker
class DailyWordWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val coreRepository: CoreRepository,
    private val dictionaryRepository: DictionaryRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            coreRepository.ensureInitialized()
            val dailyWord = dictionaryRepository.getDailyWord()
            showNotification(dailyWord.first, dailyWord.second)
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailyWordWorker", "Error in daily word work", e)
            Result.failure()
        }
    }

    private fun showNotification(word: String, synonym: String) {
        val localizedContext = LocaleHelper.wrap(applicationContext, LocaleHelper.savedTag(applicationContext))
        val channelId = "daily_word_channel"
        val notificationId = 1
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                localizedContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(localizedContext.getString(R.string.notification_title, word))
            .setContentText(localizedContext.getString(R.string.notification_body, synonym))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silently skip
        }
    }
}
