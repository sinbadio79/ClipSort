package com.clipsort.app.domain.repository

import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import kotlinx.coroutines.flow.Flow

/**
 * Contrat d'accès aux clips sauvegardés. L'implémentation concrète (Room) vit dans la couche data.
 */
interface ClipRepository {

    fun observeAllClips(): Flow<List<Clip>>

    fun observeClipsByCategory(categoryId: Long): Flow<List<Clip>>

    suspend fun saveClip(
        url: String,
        sourceApp: SourceApp,
        categoryId: Long,
        comment: String?
    ): Clip

    suspend fun updateStatus(clipId: Long, status: ClipStatus)

    suspend fun deleteClip(clipId: Long)
}
