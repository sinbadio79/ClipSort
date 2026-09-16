package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long): Category? =
        categoryRepository.getCategoryById(categoryId)
}
