package com.hayhak.turkcesozluk.di

import android.content.Context
import com.hayhak.turkcesozluk.data.db.AppDatabase
import com.hayhak.turkcesozluk.data.db.FavoriteDao
import com.hayhak.turkcesozluk.data.db.SearchHistoryDao
import com.hayhak.turkcesozluk.data.db.UserSynonymDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideUserSynonymDao(database: AppDatabase): UserSynonymDao {
        return database.userSynonymDao()
    }
}
