package com.hayhak.turkcesozluk

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hayhak.turkcesozluk.data.repository.CoreRepository
import com.hayhak.turkcesozluk.util.LocaleHelper
import com.hayhak.turkcesozluk.worker.DailyWordWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory

@HiltAndroidApp
class TurkceSozlukApp : Application(), Configuration.Provider {

    @Inject
    lateinit var coreRepository: CoreRepository

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Uygulama sureci boyunca yasar; onTerminate() gercek cihazlarda cagrilmadigi
    // icin ayrica iptal edilmesi anlamli degil.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base, LocaleHelper.savedTag(base)))
    }

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applyAppLocale(LocaleHelper.savedTag(this))
        android.util.Log.d("TurkceSozlukApp", "onCreate started")

        // WorkManager kurulumu ve CoreRepository'nin CSV/cache okuması potansiyel olarak ağır iş;
        // ana thread'i bloklamamaları için hepsini arka plana taşıyoruz (ilk frame'in bir an önce çizilmesi için).
        applicationScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                WorkManager.initialize(this@TurkceSozlukApp, workManagerConfiguration)
                android.util.Log.d("TurkceSozlukApp", "WorkManager initialized in ${System.currentTimeMillis() - startTime}ms")
            } catch (e: Exception) {
                android.util.Log.e("TurkceSozlukApp", "WorkManager initialization failed", e)
            }

            try {
                val settingsStart = System.currentTimeMillis()
                com.hayhak.turkcesozluk.data.db.SettingsManager.init(this@TurkceSozlukApp)
                android.util.Log.d("TurkceSozlukApp", "SettingsManager init in ${System.currentTimeMillis() - settingsStart}ms")

                launch {
                    android.util.Log.d("TurkceSozlukApp", "Starting CoreRepository.initialize()")
                    coreRepository.initialize()
                }

                launch {
                    android.util.Log.d("TurkceSozlukApp", "Scheduling daily notification")
                    scheduleDailyNotification()
                }
            } catch (e: Exception) {
                android.util.Log.e("TurkceSozlukApp", "Error during app initialization coroutine", e)
            }
        }
    }

    private fun scheduleDailyNotification() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWordWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("daily_word_notification")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_word_work",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
