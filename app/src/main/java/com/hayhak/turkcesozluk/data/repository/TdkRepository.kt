package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.data.model.TdkEntry
import com.hayhak.turkcesozluk.data.model.TdkLookupResult
import com.hayhak.turkcesozluk.data.model.TdkMeaning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TDK Güncel Türkçe Sözlük (sozluk.gov.tr) sorgusu.
 * Resmi public API değildir; site'nin kullandığı gts uç noktasına istek atar.
 */
@Singleton
class TdkRepository @Inject constructor() {

    suspend fun lookup(word: String): TdkLookupResult = withContext(Dispatchers.IO) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return@withContext TdkLookupResult.NotFound

        try {
            val encoded = URLEncoder.encode(trimmed, Charsets.UTF_8.name())
            val url = URL("https://sozluk.gov.tr/gts?ara=$encoded")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "TurkceSozluk/1.2 (Android)")
            }

            try {
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()

                if (code !in 200..299) {
                    return@withContext TdkLookupResult.Error("TDK yanıt vermedi ($code)")
                }
                if (body.isBlank()) {
                    return@withContext TdkLookupResult.NotFound
                }

                val trimmedBody = body.trim()
                if (trimmedBody.startsWith("{")) {
                    val obj = JSONObject(trimmedBody)
                    if (obj.has("error")) {
                        return@withContext TdkLookupResult.NotFound
                    }
                }

                val array = JSONArray(trimmedBody)
                if (array.length() == 0) {
                    return@withContext TdkLookupResult.NotFound
                }

                val entry = parseEntry(array.getJSONObject(0))
                if (entry.meanings.isEmpty()) {
                    TdkLookupResult.NotFound
                } else {
                    TdkLookupResult.Success(entry)
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "TDK lookup failed for '$trimmed'", e)
            TdkLookupResult.Error("İnternet bağlantısı yok veya TDK'ya ulaşılamadı")
        }
    }

    private fun parseEntry(obj: JSONObject): TdkEntry {
        val meanings = mutableListOf<TdkMeaning>()
        val anlamlar = obj.optJSONArray("anlamlarListe")
        if (anlamlar != null) {
            for (i in 0 until anlamlar.length()) {
                val a = anlamlar.getJSONObject(i)
                val meaningText = a.optString("anlam").trim()
                if (meaningText.isEmpty()) continue

                val property = a.optJSONArray("ozelliklerListe")
                    ?.optJSONObject(0)
                    ?.optString("tam_adi")
                    ?.takeIf { it.isNotBlank() }

                val examples = mutableListOf<String>()
                val ornekler = a.optJSONArray("orneklerListe")
                if (ornekler != null) {
                    for (j in 0 until ornekler.length()) {
                        val o = ornekler.getJSONObject(j)
                        val ornek = o.optString("ornek").trim()
                        if (ornek.isEmpty()) continue
                        val yazar = o.optJSONArray("yazar")
                            ?.optJSONObject(0)
                            ?.optString("kisa_adi")
                            ?.takeIf { it.isNotBlank() }
                        examples += if (yazar != null) "$ornek — $yazar" else ornek
                    }
                }

                meanings += TdkMeaning(
                    meaning = meaningText,
                    property = property,
                    examples = examples
                )
            }
        }

        val proverbs = mutableListOf<String>()
        val atasozu = obj.optJSONArray("atasozu")
        if (atasozu != null) {
            for (i in 0 until atasozu.length()) {
                val m = atasozu.getJSONObject(i).optString("madde").trim()
                if (m.isNotEmpty()) proverbs += m
            }
        }

        val compounds = obj.optString("birlesikler")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return TdkEntry(
            word = obj.optString("madde").ifBlank { obj.optString("madde_duz") },
            origin = obj.optString("lisan").takeIf { it.isNotBlank() },
            pronunciation = obj.optString("telaffuz").takeIf { it.isNotBlank() },
            meanings = meanings,
            proverbs = proverbs.take(8),
            compounds = compounds.take(12)
        )
    }

    private companion object {
        const val TAG = "TdkRepository"
    }
}
