package com.hayhak.turkcesozluk.data.model

data class TdkMeaning(
    val meaning: String,
    val property: String? = null,
    val examples: List<String> = emptyList()
)

data class TdkEntry(
    val word: String,
    val origin: String? = null,
    val pronunciation: String? = null,
    val meanings: List<TdkMeaning> = emptyList(),
    val proverbs: List<String> = emptyList(),
    val compounds: List<String> = emptyList()
)

sealed class TdkLookupResult {
    data class Success(val entry: TdkEntry) : TdkLookupResult()
    data object NotFound : TdkLookupResult()
    data class Error(val message: String) : TdkLookupResult()
}
