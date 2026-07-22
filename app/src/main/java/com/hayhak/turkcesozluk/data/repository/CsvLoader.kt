package com.hayhak.turkcesozluk.data.repository

import android.content.Context
import android.util.Log
import com.hayhak.turkcesozluk.data.model.WordDefinition
import com.hayhak.turkcesozluk.util.normalizeTR
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_VERSION = 2

@Singleton
class CsvLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Yüklenen verileri ve hangi kelimelerin 'ana' kelime olduğunu döndürür
    data class LoadResult(
        val data: Map<String, Set<String>>,
        val primaryWords: Set<String>,
        val sortedKeys: List<String>,
        val sortedPrimary: List<String>
    )

    /**
     * CSV'yi elle (karakter karakter) ayrıştırmak büyük dosyalarda (özellikle ART henüz
     * JIT ısınmamışken, yani ilk aşımlarda) çok yavaş; bu yüzden ayrıştırılmış sonucu
     * diske önbelleğe alıp sonraki açılışlarda oradan okuyoruz.
     */
    fun loadFromAssetsCached(mode: com.hayhak.turkcesozluk.data.db.DictionaryMode): LoadResult {
        val cacheFile = cacheFileFor("dict_${mode.name}")
        readLoadResultCache(cacheFile)?.let { return it }
        val result = loadFromAssets(mode)
        writeLoadResultCache(cacheFile, result)
        return result
    }

    fun loadDefinitionsCached(): Map<String, WordDefinition> {
        val cacheFile = cacheFileFor("definitions")
        readDefinitionsCache(cacheFile)?.let { return it }
        val result = loadDefinitions()
        writeDefinitionsCache(cacheFile, result)
        return result
    }

    private fun cacheFileFor(name: String): File = File(context.cacheDir, "$name.v$CACHE_VERSION.bin")

    private fun readLoadResultCache(file: File): LoadResult? {
        if (!file.exists()) return null
        return try {
            DataInputStream(BufferedInputStream(file.inputStream(), 65536)).use { input ->
                val primaryCount = input.readInt()
                val primaryWords = HashSet<String>(primaryCount * 4 / 3 + 1)
                repeat(primaryCount) { primaryWords.add(input.readUTF()) }
                
                val mapSize = input.readInt()
                val map = HashMap<String, Set<String>>(mapSize * 4 / 3 + 1)
                repeat(mapSize) {
                    val key = input.readUTF()
                    val meaningsCount = input.readInt()
                    val meanings = HashSet<String>(meaningsCount * 4 / 3 + 1)
                    repeat(meaningsCount) { meanings.add(input.readUTF()) }
                    map[key] = meanings
                }

                val sortedKeysSize = input.readInt()
                val sortedKeys = ArrayList<String>(sortedKeysSize)
                repeat(sortedKeysSize) { sortedKeys.add(input.readUTF()) }

                val sortedPrimarySize = input.readInt()
                val sortedPrimary = ArrayList<String>(sortedPrimarySize)
                repeat(sortedPrimarySize) { sortedPrimary.add(input.readUTF()) }

                LoadResult(map, primaryWords, sortedKeys, sortedPrimary)
            }
        } catch (e: Exception) {
            file.delete()
            null
        }
    }

    private fun writeLoadResultCache(file: File, result: LoadResult) {
        try {
            DataOutputStream(BufferedOutputStream(file.outputStream(), 65536)).use { output ->
                output.writeInt(result.primaryWords.size)
                result.primaryWords.forEach { output.writeUTF(it) }
                output.writeInt(result.data.size)
                result.data.forEach { (key, meanings) ->
                    output.writeUTF(key)
                    output.writeInt(meanings.size)
                    meanings.forEach { output.writeUTF(it) }
                }
                output.writeInt(result.sortedKeys.size)
                result.sortedKeys.forEach { output.writeUTF(it) }
                output.writeInt(result.sortedPrimary.size)
                result.sortedPrimary.forEach { output.writeUTF(it) }
            }
        } catch (e: Exception) {
            file.delete()
        }
    }

    private fun readDefinitionsCache(file: File): Map<String, WordDefinition>? {
        if (!file.exists()) return null
        return try {
            DataInputStream(BufferedInputStream(file.inputStream())).use { input ->
                val size = input.readInt()
                val map = HashMap<String, WordDefinition>(size)
                repeat(size) {
                    val word = input.readUTF()
                    val meaningsCount = input.readInt()
                    val meanings = ArrayList<String>(meaningsCount)
                    repeat(meaningsCount) { meanings.add(input.readUTF()) }
                    val origin = if (input.readBoolean()) input.readUTF() else null
                    val pronunciation = if (input.readBoolean()) input.readUTF() else null
                    map[word] = WordDefinition(word, meanings, origin, pronunciation)
                }
                map
            }
        } catch (e: Exception) {
            file.delete()
            null
        }
    }

    private fun writeDefinitionsCache(file: File, definitions: Map<String, WordDefinition>) {
        try {
            DataOutputStream(BufferedOutputStream(file.outputStream())).use { output ->
                output.writeInt(definitions.size)
                definitions.values.forEach { def ->
                    output.writeUTF(def.word)
                    output.writeInt(def.meanings.size)
                    def.meanings.forEach { output.writeUTF(it) }
                    output.writeBoolean(def.origin != null)
                    def.origin?.let { output.writeUTF(it) }
                    output.writeBoolean(def.pronunciation != null)
                    def.pronunciation?.let { output.writeUTF(it) }
                }
            }
        } catch (e: Exception) {
            file.delete()
        }
    }

    fun loadFromAssets(mode: com.hayhak.turkcesozluk.data.db.DictionaryMode): LoadResult {
        if (mode == com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL) {
            val combinedMap = mutableMapOf<String, MutableSet<String>>()
            val combinedPrimary = mutableSetOf<String>()
            
            com.hayhak.turkcesozluk.data.db.DictionaryMode.entries.filter { it != com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL }.forEach { subMode ->
                val result = loadFromAssets(subMode)
                result.primaryWords.forEach { combinedPrimary.add(it) }
                result.data.forEach { (word, meanings) ->
                    combinedMap.getOrPut(word) { mutableSetOf() }.addAll(meanings)
                }
            }
            return LoadResult(
                combinedMap.mapValues { it.value.toSet() }, 
                combinedPrimary,
                combinedMap.keys.toList().sorted(),
                combinedPrimary.toList().sorted()
            )
        }

        val tempMap = mutableMapOf<String, MutableSet<String>>()
        val primaryWords = mutableSetOf<String>()
        
        val (fileName, applyFilter) = when (mode) {
            com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS -> "es_anlamlilar.csv" to true
            com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS -> "fiiller.csv" to false
            com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS -> "isimler.csv" to false
            com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS -> "deyimler.csv" to false
            com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES -> "sifatlar.csv" to false
            else -> throw IllegalArgumentException("Mode ALL should be handled separately")
        }
        
        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readLine() // Header skip
                    reader.forEachLine { line ->
                        if (line.isBlank()) return@forEachLine
                        
                        val parts = parseCsvLine(line)
                        if (parts.size >= 2) {
                            val word1 = parts[0].trim().normalizeTR()
                            val fullWord2 = parts[1].trim().normalizeTR()
                            
                            if (word1.isNotEmpty() && fullWord2.isNotEmpty()) {
                                // Word1 her zaman bir ana kelimedir (fiil veya kelime)
                                primaryWords.add(word1)

                                val meanings = fullWord2.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                                
                                meanings.forEach { meaning ->
                                    val isShort = meaning.split(" ").size <= 3
                                    val isValid = if (applyFilter) isShort else true

                                    if (isValid) {
                                        tempMap.getOrPut(word1) { mutableSetOf() }.add(meaning)
                                        
                                        // İsimler/Tanımlar modunda ters eşleme yapma (tanımdan kelimeye gitme)
                                        if (mode != com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS) {
                                            tempMap.getOrPut(meaning) { mutableSetOf() }.add(word1)
                                        }
                                        
                                        // Eş anlamlılarda her iki kelime de kısa ise ikisi de ana kelime olabilir
                                        if (applyFilter && isShort) {
                                            primaryWords.add(meaning)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                Log.e("CsvLoader", "CSV Load Error ($fileName)", e)
            }
        }
        val sortedKeys = tempMap.keys.toList().sorted()
        val sortedPrimaryList = primaryWords.toList().sorted()
        return LoadResult(tempMap.mapValues { it.value.toSet() }, primaryWords, sortedKeys, sortedPrimaryList)
    }

    fun loadDefinitions(): Map<String, WordDefinition> {
        val result = mutableMapOf<String, WordDefinition>()
        try {
            context.assets.open("isimler.csv").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readLine() // header skip
                    reader.forEachLine { line ->
                        if (line.isBlank()) return@forEachLine
                        val parts = parseCsvLine(line)
                        if (parts.size < 2) return@forEachLine
                        val word = parts[0].trim().normalizeTR()
                        val meaningsRaw = parts[1].trim()
                        if (word.isEmpty() || meaningsRaw.isEmpty()) return@forEachLine
                        val meanings = meaningsRaw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val origin = parts.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() }
                        val pronunciation = parts.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() }
                        val existing = result[word]
                        result[word] = if (existing != null) {
                            existing.copy(meanings = existing.meanings + meanings)
                        } else {
                            WordDefinition(word, meanings, origin, pronunciation)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                Log.e("CsvLoader", "İsimler CSV yükleme hatası", e)
            }
        }
        return result
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        var current = StringBuilder()
        var i = 0
        while (i < line.length) {
            val char = line[i]
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString())
                current = StringBuilder()
            } else {
                current.append(char)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
