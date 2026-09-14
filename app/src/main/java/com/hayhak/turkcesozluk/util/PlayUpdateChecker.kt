package com.hayhak.turkcesozluk.util

import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.hayhak.turkcesozluk.BuildConfig
import kotlinx.coroutines.tasks.await

data class PlayUpdateInfo(
    val currentVersionCode: Int,
    val availableVersionCode: Int,
) {
    val isOutdated: Boolean get() = availableVersionCode > currentVersionCode
}

enum class UpdateCheckStatus {
    AVAILABLE,
    UP_TO_DATE,
    UNAVAILABLE,
}

data class PlayUpdateCheckResult(
    val status: UpdateCheckStatus,
    val info: PlayUpdateInfo? = null,
)

/**
 * Google Play In-App Updates API ile güncelleme kontrolü.
 * Yalnızca Play Store'dan yüklenen kurulumlarda çalışır.
 */
object PlayUpdateChecker {
    private const val TAG = "PlayUpdateChecker"
    private const val PREFS = "app_settings"
    private const val KEY_DISMISSED_UPDATE_CODE = "dismissed_update_version_code"

    suspend fun checkDetailed(context: Context): PlayUpdateCheckResult {
        return try {
            val manager = AppUpdateManagerFactory.create(context.applicationContext)
            val info = manager.appUpdateInfo.await()
            val availability = info.updateAvailability()
            val availableCode = info.availableVersionCode()
            val currentCode = BuildConfig.VERSION_CODE

            Log.d(TAG, "availability=$availability current=$currentCode available=$availableCode")

            if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                val typeAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ||
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                if (typeAllowed && availableCode > currentCode) {
                    return PlayUpdateCheckResult(
                        status = UpdateCheckStatus.AVAILABLE,
                        info = PlayUpdateInfo(currentCode, availableCode),
                    )
                }
            }

            PlayUpdateCheckResult(status = UpdateCheckStatus.UP_TO_DATE)
        } catch (e: Exception) {
            Log.d(TAG, "Update check failed (expected for sideload/debug): ${e.message}")
            PlayUpdateCheckResult(status = UpdateCheckStatus.UNAVAILABLE)
        }
    }

    /**
     * Silent startup check: only when online; skips versions the user already dismissed.
     */
    suspend fun checkForStartupPrompt(context: Context): PlayUpdateInfo? {
        if (!NetworkUtils.isOnline(context)) {
            Log.d(TAG, "Startup update check skipped: offline")
            return null
        }
        val result = checkDetailed(context)
        val info = result.info ?: return null
        if (result.status != UpdateCheckStatus.AVAILABLE || !info.isOutdated) return null
        val dismissed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_DISMISSED_UPDATE_CODE, 0)
        if (info.availableVersionCode <= dismissed) {
            Log.d(TAG, "Startup update prompt skipped: dismissed for ${info.availableVersionCode}")
            return null
        }
        return info
    }

    fun markUpdateDismissed(context: Context, availableVersionCode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DISMISSED_UPDATE_CODE, availableVersionCode)
            .apply()
    }
}
