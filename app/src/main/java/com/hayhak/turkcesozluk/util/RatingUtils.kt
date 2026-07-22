package com.hayhak.turkcesozluk.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Play Store'un yerleşik yıldız değerlendirme kutusunu uygulamadan çıkmadan gösterir.
 * Google kota nedeniyle akışı göstermeyebilir; bu durumda Play Store uygulama sayfası açılır.
 */
fun Activity.launchInAppReview() {
    val manager = ReviewManagerFactory.create(this)
    val requestTask = manager.requestReviewFlow()
    requestTask.addOnCompleteListener { request ->
        if (request.isSuccessful) {
            manager.launchReviewFlow(this, request.result)
        } else {
            openPlayStoreListing()
        }
    }
}

private fun Activity.openPlayStoreListing() {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}
