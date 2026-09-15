package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Expose le flux réactif des catégories avec leur nombre de clips, pour l'écran bibliothèque.
 */
class GetCategoriesWithCountUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryWithCount>> =
        categoryRepository.observeCategoriesWithCount()
}
