package com.hayhak.turkcesozluk.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "study_records", primaryKeys = ["kind", "mode", "word", "answer"])
data class StudyRecord(
    val kind: String,
    val mode: String,
    val word: String,
    val answer: String,
    val options: String = "[]",
    val streak: Int = 0,
    val dueAt: Long = 0,
    val updatedAt: Long = 0,
)

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_records")
    fun observe(): Flow<List<StudyRecord>>
    @Query("SELECT * FROM study_records")
    suspend fun all(): List<StudyRecord>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(record: StudyRecord)
    @Delete
    suspend fun delete(record: StudyRecord)
}

object ReviewSchedule {
    fun next(previous: Int, known: Boolean, now: Long): Pair<Int, Long> {
        if (!known) return 0 to now + 10 * 60_000L
        val streak = (previous + 1).coerceIn(1, 6)
        val days = intArrayOf(1, 3, 7, 14, 30, 60)[streak - 1]
        return streak to now + days * 86_400_000L
    }
}
