package com.hayhak.turkcesozluk.data.model

data class WordDefinition(
    val word: String,
    val meanings: List<String>,
    val origin: String?,
    val pronunciation: String?
)
