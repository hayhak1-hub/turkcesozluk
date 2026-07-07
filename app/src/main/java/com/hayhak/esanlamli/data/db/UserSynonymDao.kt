package com.hayhak.esanlamli.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSynonymDao {
    @Query("SELECT * FROM user_synonyms")
    fun getAllUserSynonyms(): Flow<List<UserSynonym>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSynonym(userSynonym: UserSynonym)

    @Delete
    suspend fun deleteUserSynonym(userSynonym: UserSynonym)
}
