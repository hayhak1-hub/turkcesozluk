package com.hayhak.esanlamli

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hayhak.esanlamli.data.billing.PremiumManager
import com.hayhak.esanlamli.data.repository.CoreRepository
import com.hayhak.esanlamli.worker.DailyWordWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory

@HiltAndroidApp
class EsAnlamliApp : Application(), Configuration.Provider {

    @Inject
    lateinit var coreRepository: CoreRepository
    
    @Inject
    lateinit var premiumManager: PremiumManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // WorkManager'ı manuel başlatmayı deneyelim (Hilt ile çakışmayı önlemek için)
        // androidx.work.WorkManager.initialize(this, workManagerConfiguration)

        premiumManager.start()

        applicationScope.launch(Dispatchers.IO) {
            com.hayhak.esanlamli.data.db.SettingsManager.init(this@EsAnlamliApp)
            coreRepository.initialize()
            scheduleDailyNotification()
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

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}
