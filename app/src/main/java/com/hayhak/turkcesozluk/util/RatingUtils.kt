package com.hayhak.turkcesozluk.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hayhak.turkcesozluk.R

/**
 * "Bizi Değerlendirin" — Play Store uygulama sayfasını açar.
 * Wifi Guard ile aynı yaklaşım: [PlayStoreHelper.openListing].
 */
fun Context.launchInAppReview() {
    PlayStoreHelper.openListing(this)
}
