package com.clipsort.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clipsort.app.data.local.dao.CategoryDao
import com.clipsort.app.data.local.dao.ClipDao
import com.clipsort.app.data.local.entity.CategoryEntity
import com.clipsort.app.data.local.entity.ClipEntity

@Database(
    entities = [CategoryEntity::class, ClipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun clipDao(): ClipDao

    companion object {
        const val DATABASE_NAME = "clipsort.db"
    }
}
