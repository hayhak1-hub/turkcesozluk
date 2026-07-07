package com.hayhak.esanlamli.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_synonyms")
data class UserSynonym(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val synonym: String
)
