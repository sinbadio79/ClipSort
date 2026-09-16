package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Supprime une catégorie. La contrainte CASCADE au niveau de la base de données
 * supprime automatiquement les clips qu'elle contenait.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> =
        runCatching { categoryRepository.deleteCategory(categoryId) }
}
