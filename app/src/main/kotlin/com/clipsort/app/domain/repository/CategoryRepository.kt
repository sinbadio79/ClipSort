package com.clipsort.app.domain.repository

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.CategoryWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Contrat d'accès aux catégories. L'implémentation concrète (Room) vit dans la couche data ;
 * le domain et la presentation ne dépendent que de cette interface.
 */
interface CategoryRepository {

    fun observeCategoriesWithCount(): Flow<List<CategoryWithCount>>

    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategoryById(categoryId: Long): Category?

    suspend fun createCategory(name: String, colorHex: String): Category

    suspend fun deleteCategory(categoryId: Long)

    suspend fun renameCategory(categoryId: Long, newName: String)
}
