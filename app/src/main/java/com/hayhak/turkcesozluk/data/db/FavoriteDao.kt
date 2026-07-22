package com.hayhak.turkcesozluk.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWord: FavoriteWord)

    @Delete
    suspend fun deleteFavorite(favoriteWord: FavoriteWord)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE word = :word LIMIT 1)")
    fun isFavorite(word: String): Flow<Boolean>
}
