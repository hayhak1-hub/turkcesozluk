package com.hayhak.turkcesozluk.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hayhak.turkcesozluk.R

object PlayStoreHelper {
    fun openListing(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_rate_open_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.settings_link_open_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareApp(context: Context) {
        val playUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val body = context.getString(R.string.settings_share_text, playUrl)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.settings_share))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
