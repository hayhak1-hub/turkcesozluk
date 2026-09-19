package com.hayhak.turkcesozluk.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteWord::class, SearchHistory::class, UserSynonym::class, StudyRecord::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun userSynonymDao(): UserSynonymDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `study_records` (`kind` TEXT NOT NULL, `mode` TEXT NOT NULL, `word` TEXT NOT NULL, `answer` TEXT NOT NULL, `options` TEXT NOT NULL, `streak` INTEGER NOT NULL, `dueAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`kind`, `mode`, `word`, `answer`))")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `search_history` " +
                    "(`word` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`word`))"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQLite'da AUTOINCREMENT sadece INTEGER PRIMARY KEY ile kullanılır
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_synonyms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `synonym` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `favorites_temp` (`word` TEXT NOT NULL, `synonym` TEXT NOT NULL, PRIMARY KEY(`word`, `synonym`))")
                db.execSQL("INSERT OR IGNORE INTO `favorites_temp` (word, synonym) SELECT word, synonym FROM favorite_words")
                db.execSQL("DROP TABLE IF EXISTS favorite_words")
                db.execSQL("DROP TABLE IF EXISTS favorites")
                db.execSQL("ALTER TABLE favorites_temp RENAME TO favorites")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esanlamli_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
