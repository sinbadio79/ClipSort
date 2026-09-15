package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.domain.repository.ClipRepository
import javax.inject.Inject

/**
 * Enregistre un clip après catégorisation par l'utilisateur.
 * Rejette explicitement les entrées invalides plutôt que de laisser
 * la couche data ou l'UI gérer une règle métier.
 */
class SaveClipUseCase @Inject constructor(
    private val clipRepository: ClipRepository
) {

    suspend operator fun invoke(
        url: String,
        sourceApp: SourceApp,
        categoryId: Long,
        comment: String?
    ): Result<Clip> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("L'URL du clip ne peut pas être vide."))
        }
        if (categoryId <= 0L) {
            return Result.failure(IllegalArgumentException("Une catégorie valide doit être sélectionnée."))
        }

        val trimmedComment = comment?.trim()?.takeIf { it.isNotEmpty() }

        return runCatching {
            clipRepository.saveClip(
                url = url.trim(),
                sourceApp = sourceApp,
                categoryId = categoryId,
                comment = trimmedComment
            )
        }
    }
}
