package com.hayhak.turkcesozluk

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hayhak.turkcesozluk.data.db.*
import com.hayhak.turkcesozluk.data.repository.BackupRepository
import com.hayhak.turkcesozluk.data.repository.DictionaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test fun roundTripMergesWithoutDuplicatingOrReplacingNewerProgress() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val file = File.createTempFile("backup-test", ".json", context.cacheDir)
        try {
            val repository = BackupRepository(context, db, DictionaryRepository())
            val favorite = FavoriteWord("okul", "mektep")
            val record = StudyRecord("review", "", "okul", "mektep", streak = 1, dueAt = 100, updatedAt = 10)
            db.favoriteDao().insertFavorite(favorite)
            db.studyDao().put(record)
            repository.export(Uri.fromFile(file))
            db.clearAllTables()
            val newer = record.copy(streak = 2, dueAt = 200, updatedAt = 20)
            db.studyDao().put(newer)
            repository.restore(Uri.fromFile(file))
            repository.restore(Uri.fromFile(file))
            assertEquals(listOf(favorite), db.favoriteDao().getAllFavorites().first())
            assertEquals(listOf(newer), db.studyDao().all())
        } finally {
            db.close()
            file.delete()
        }
    }

    @Test fun invalidBackupLeavesExistingRecordsUntouched() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val file = File.createTempFile("backup-invalid", ".json", context.cacheDir)
        try {
            val favorite = FavoriteWord("okul", "mektep")
            db.favoriteDao().insertFavorite(favorite)
            file.writeText("""{"format":"turkcesozluk-backup","version":999}""")
            try {
                BackupRepository(context, db, DictionaryRepository()).restore(Uri.fromFile(file))
                fail("Invalid version must be rejected")
            } catch (_: IllegalArgumentException) { }
            assertEquals(listOf(favorite), db.favoriteDao().getAllFavorites().first())
        } finally {
            db.close()
            file.delete()
        }
    }
}
