package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.repository.CategoryRepository
import javax.inject.Inject

class RenameCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, newName: String): Result<Unit> {
        val trimmedName = newName.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Le nom de la catégorie ne peut pas être vide."))
        }
        return runCatching { categoryRepository.renameCategory(categoryId, trimmedName) }
    }
}
