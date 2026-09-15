package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * Crée une nouvelle catégorie libre. Si aucune couleur n'est fournie, une couleur
 * est assignée automatiquement depuis une palette fixe (cohérence visuelle du design system).
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(name: String, colorHex: String? = null): Result<Category> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Le nom de la catégorie ne peut pas être vide."))
        }

        val resolvedColor = colorHex ?: DEFAULT_PALETTE.random(Random(trimmedName.hashCode()))

        return runCatching {
            categoryRepository.createCategory(name = trimmedName, colorHex = resolvedColor)
        }
    }

    private companion object {
        val DEFAULT_PALETTE = listOf(
            "#378ADD", "#639922", "#D85A30", "#7F77DD", "#D4537E", "#1D9E75"
        )
    }
}
