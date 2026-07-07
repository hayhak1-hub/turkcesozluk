package com.hayhak.esanlamli.data.db

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["word", "synonym"])
data class FavoriteWord(
    val word: String,
    val synonym: String
)
