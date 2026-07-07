package com.hayhak.esanlamli.data.repository

import android.content.Context
import android.util.Log
import com.hayhak.esanlamli.util.normalizeTR
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Yüklenen verileri ve hangi kelimelerin 'ana' kelime olduğunu döndürür
    data class LoadResult(
        val data: Map<String, Set<String>>,
        val primaryWords: Set<String>
    )

    fun loadFromAssets(mode: com.hayhak.esanlamli.data.db.DictionaryMode): LoadResult {
        val tempMap = mutableMapOf<String, MutableSet<String>>()
        val primaryWords = mutableSetOf<String>()
        
        val (fileName, applyFilter) = when (mode) {
            com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS -> "es_anlamlilar.csv" to true
            com.hayhak.esanlamli.data.db.DictionaryMode.VERBS -> "fiiller.csv" to false
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
                                        tempMap.getOrPut(meaning) { mutableSetOf() }.add(word1)
                                        
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
            Log.e("CsvLoader", "CSV Load Error ($fileName)", e)
        }
        return LoadResult(tempMap.mapValues { it.value.toSet() }, primaryWords)
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
