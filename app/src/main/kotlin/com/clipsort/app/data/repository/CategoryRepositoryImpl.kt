package com.clipsort.app.data.repository

import com.clipsort.app.data.local.dao.CategoryDao
import com.clipsort.app.data.mapper.toDomain
import com.clipsort.app.data.mapper.toEntity
import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategoriesWithCount(): Flow<List<CategoryWithCount>> =
        categoryDao.observeCategoriesWithCount().map { rows -> rows.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createCategory(name: String, colorHex: String): Category {
        val category = Category(name = name, colorHex = colorHex, createdAt = System.currentTimeMillis())
        val generatedId = categoryDao.insert(category.toEntity())
        return category.copy(id = generatedId)
    }

    override suspend fun deleteCategory(categoryId: Long) {
        val entity = categoryDao.getById(categoryId) ?: return
        categoryDao.delete(entity)
    }

    override suspend fun renameCategory(categoryId: Long, newName: String) {
        val entity = categoryDao.getById(categoryId) ?: return
        categoryDao.update(entity.copy(name = newName))
    }
}
