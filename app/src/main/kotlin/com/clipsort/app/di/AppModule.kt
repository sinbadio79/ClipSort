package com.clipsort.app.di

import android.content.Context
import androidx.room.Room
import com.clipsort.app.data.local.AppDatabase
import com.clipsort.app.data.local.dao.CategoryDao
import com.clipsort.app.data.local.dao.ClipDao
import com.clipsort.app.data.repository.CategoryRepositoryImpl
import com.clipsort.app.data.repository.ClipRepositoryImpl
import com.clipsort.app.domain.repository.CategoryRepository
import com.clipsort.app.domain.repository.ClipRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration() // acceptable en phase MVP, à retirer avant une v2 avec données à préserver
            .build()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideClipDao(database: AppDatabase): ClipDao = database.clipDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindClipRepository(impl: ClipRepositoryImpl): ClipRepository
}
