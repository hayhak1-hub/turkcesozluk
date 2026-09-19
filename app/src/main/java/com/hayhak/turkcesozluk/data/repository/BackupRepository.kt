package com.hayhak.turkcesozluk.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.hayhak.turkcesozluk.data.db.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Portable, versioned user-data backup. No credentials or bundled dictionary data. */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val dictionary: DictionaryRepository,
) {
    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val root = db.withTransaction {
            JSONObject().put("format", "turkcesozluk-backup").put("version", 1)
                .put("favorites", JSONArray().apply { db.favoriteDao().getAllFavorites().first().forEach {
                    put(JSONObject().put("word", it.word).put("answer", it.synonym))
                } })
                .put("words", JSONArray().apply { db.userSynonymDao().getAllUserSynonyms().first().forEach {
                    put(JSONObject().put("word", it.word).put("answer", it.synonym))
                } })
                .put("history", JSONArray().apply { db.searchHistoryDao().all().forEach {
                    put(JSONObject().put("word", it.word).put("timestamp", it.timestamp))
                } })
                .put("study", JSONArray().apply { db.studyDao().all().forEach {
                    put(JSONObject().put("kind", it.kind).put("mode", it.mode).put("word", it.word)
                        .put("answer", it.answer).put("options", JSONArray(it.options))
                        .put("streak", it.streak).put("dueAt", it.dueAt).put("updatedAt", it.updatedAt))
                } })
        }
        root.put("stats", JSONObject(UserStatsManager.exportStats(context)))
        val bytes = root.toString(2).toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_BYTES)
        checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(bytes) }
    }

    suspend fun restore(uri: Uri) = withContext(Dispatchers.IO) {
        val bytes = checkNotNull(context.contentResolver.openInputStream(uri)).use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                require(output.size() + n <= MAX_BYTES)
                output.write(buffer, 0, n)
            }
            output.toByteArray()
        }
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        require(root.getString("format") == "turkcesozluk-backup" && root.getInt("version") == 1)
        fun objects(key: String): List<JSONObject> {
            val array = root.getJSONArray(key)
            require(array.length() <= 50_000)
            return List(array.length()) { array.getJSONObject(it) }
        }
        fun text(o: JSONObject, key: String): String = o.getString(key).also {
            require(it.isNotBlank() && it.length <= 20_000)
        }
        val favorites = objects("favorites").map { FavoriteWord(text(it, "word"), text(it, "answer")) }
        val words = objects("words").map { UserSynonym(word = text(it, "word"), synonym = text(it, "answer")) }
        val history = objects("history").map { SearchHistory(text(it, "word"), it.getLong("timestamp").also { n -> require(n >= 0) }) }
        val records = objects("study").map {
            val kind = it.getString("kind")
            val mode = it.getString("mode")
            require(kind in setOf("review", "mistake"))
            require(if (kind == "review") mode.isEmpty() else DictionaryMode.entries.any { m -> m.name == mode })
            val options = it.getJSONArray("options")
            val answer = text(it, "answer")
            if (kind == "mistake") {
                require(options.length() == 4)
                val labels = List(4) { i -> options.getString(i).also { s -> require(s.isNotBlank() && s.length <= 20_000) } }
                require(labels.distinct().size == 4 && answer in labels)
            }
            StudyRecord(kind, mode, text(it, "word"), answer, options.toString(),
                it.getInt("streak").also { n -> require(n in 0..6) },
                it.getLong("dueAt").also { n -> require(n >= 0) },
                it.getLong("updatedAt").also { n -> require(n >= 0) })
        }
        val statsObject = root.getJSONObject("stats")
        val stats = mutableMapOf<String, Any>()
        statsObject.keys().forEach { key ->
            when {
                key == "last_activity_date" -> stats[key] = statsObject.getString(key).also { require(it.isEmpty() || it.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) }
                key == "streak_count" || key.matches(Regex("daily_stat_\\d{4}-\\d{2}-\\d{2}")) ->
                    stats[key] = statsObject.getInt(key).also { require(it in 0..10_000_000) }
                else -> error("Unknown statistics field")
            }
        }
        // Validate everything before touching the database. Merge, never erase.
        db.withTransaction {
            favorites.forEach { db.favoriteDao().insertFavorite(it) }
            val existingWords = db.userSynonymDao().getAllUserSynonyms().first().map { it.word to it.synonym }.toMutableSet()
            words.forEach { if (existingWords.add(it.word to it.synonym)) db.userSynonymDao().insertUserSynonym(it) }
            val existingHistory = db.searchHistoryDao().all().associateBy { it.word }
            history.sortedBy { it.timestamp }.forEach { if (it.timestamp > (existingHistory[it.word]?.timestamp ?: -1)) db.searchHistoryDao().insertSearch(it) }
            val existingRecords = db.studyDao().all().associateBy { listOf(it.kind, it.mode, it.word, it.answer) }
            records.sortedBy { it.updatedAt }.forEach { if (it.updatedAt > (existingRecords[listOf(it.kind, it.mode, it.word, it.answer)]?.updatedAt ?: -1)) db.studyDao().put(it) }
        }
        UserStatsManager.mergeStats(context, stats)
        dictionary.addWordsManual(words.map { it.word to it.synonym })
    }

    companion object { const val MAX_BYTES = 10 * 1024 * 1024 }
}
