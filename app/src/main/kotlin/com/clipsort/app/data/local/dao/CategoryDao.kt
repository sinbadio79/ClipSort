package com.clipsort.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clipsort.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

data class CategoryWithCountRow(
    val id: Long,
    val name: String,
    val colorHex: String,
    val createdAt: Long,
    val clipCount: Int
)

@Dao
interface CategoryDao {

    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.colorHex AS colorHex, c.createdAt AS createdAt,
               COUNT(cl.id) AS clipCount
        FROM categories c
        LEFT JOIN clips cl ON cl.categoryId = c.id
        GROUP BY c.id
        ORDER BY c.createdAt DESC
        """
    )
    fun observeCategoriesWithCount(): Flow<List<CategoryWithCountRow>>

    @Query("SELECT * FROM categories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getById(categoryId: Long): CategoryEntity?
}
