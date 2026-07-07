package com.hayhak.esanlamli.data.model

data class WordDefinition(
    val word: String,
    val meanings: List<String>,
    val origin: String?,
    val pronunciation: String?
)
