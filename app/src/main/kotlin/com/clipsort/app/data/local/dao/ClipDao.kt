package com.clipsort.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clipsort.app.data.local.entity.ClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clips ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ClipEntity>>

    @Query("UPDATE clips SET comment = :comment WHERE id = :clipId")
    suspend fun updateComment(clipId: Long, comment: String?)

    @Query("UPDATE clips SET status = :status WHERE id = :clipId")
    suspend fun updateStatus(clipId: Long, status: String)

    @Query("SELECT * FROM clips WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun observeByCategory(categoryId: Long): Flow<List<ClipEntity>>

    @Insert
    suspend fun insert(clip: ClipEntity): Long

    @Update
    suspend fun update(clip: ClipEntity)

    @Delete
    suspend fun delete(clip: ClipEntity)

    @Query("SELECT * FROM clips WHERE id = :clipId LIMIT 1")
    suspend fun getById(clipId: Long): ClipEntity?
}
