package com.hayhak.esanlamli.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hayhak.esanlamli.MainActivity
import com.hayhak.esanlamli.data.repository.CoreRepository
import com.hayhak.esanlamli.data.repository.DictionaryRepository
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
        coreRepository.initialize()
        val dailyWord = dictionaryRepository.getDailyWord()
        showNotification(dailyWord.first, dailyWord.second)
        return Result.success()
    }

    private fun showNotification(word: String, synonym: String) {
        val channelId = "daily_word_channel"
        val notificationId = 1
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Günün Kelimesi", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Günün Kelimesi: $word")
            .setContentText("Eş anlamlısı: $synonym. Hemen öğren!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            // Permission might be missing or other error
        }
    }
}
