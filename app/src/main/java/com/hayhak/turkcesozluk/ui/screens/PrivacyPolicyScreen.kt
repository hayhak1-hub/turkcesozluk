package com.hayhak.turkcesozluk.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.AppLocale
import com.hayhak.turkcesozluk.data.db.SettingsManager
import org.json.JSONObject

private const val PRIVACY_POLICY_ASSET = "file:///android_asset/privacy-policy.html"

internal fun resolvePrivacyLangTag(language: AppLocale, configuration: Configuration): String {
    if (language.tag.isNotEmpty()) return normalizePrivacyLangTag(language.tag)

    val deviceTag = configuration.locales[0]?.toLanguageTag().orEmpty()
    if (deviceTag.isNotEmpty()) return normalizePrivacyLangTag(deviceTag)

    return "tr"
}

private fun normalizePrivacyLangTag(tag: String): String {
    val supported = AppLocale.entries
        .map { it.tag }
        .filter { it.isNotEmpty() }
        .toSet()

    if (tag in supported) return tag
    if (tag.startsWith("tr")) return "tr"

    val primary = tag.substringBefore('-')
    if (primary in supported) return primary

    return "en"
}

private fun renderPrivacyPolicyJs(langTag: String): String =
    "renderPrivacyPolicy(${JSONObject.quote(langTag)})"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val language by SettingsManager.languageState.collectAsState()
    val langTag = remember(language, configuration) {
        resolvePrivacyLangTag(language, configuration)
    }
    val renderScript = remember(langTag) { renderPrivacyPolicyJs(langTag) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(stringResource(R.string.settings_privacy_policy)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    @SuppressLint("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            view?.evaluateJavascript(renderScript, null)
                        }
                    }
                    loadUrl(PRIVACY_POLICY_ASSET)
                }
            },
            update = { webView ->
                webView.evaluateJavascript(renderScript, null)
            }
        )
    }
}
