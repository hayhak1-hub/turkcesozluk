package com.hayhak.esanlamli.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val word: String,
    val timestamp: Long = System.currentTimeMillis()
)
