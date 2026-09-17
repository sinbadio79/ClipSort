package com.clipsort.app.data.repository

import com.clipsort.app.data.local.dao.ClipDao
import com.clipsort.app.data.mapper.toDomain
import com.clipsort.app.data.mapper.toEntity
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.domain.repository.ClipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipRepositoryImpl @Inject constructor(
    private val clipDao: ClipDao
) : ClipRepository {

    override fun observeAllClips(): Flow<List<Clip>> =
        clipDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeClipsByCategory(categoryId: Long): Flow<List<Clip>> =
        clipDao.observeByCategory(categoryId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveClip(
        url: String,
        sourceApp: SourceApp,
        categoryId: Long,
        comment: String?
    ): Clip {
        val clip = Clip(
            url = url,
            sourceApp = sourceApp,
            categoryId = categoryId,
            comment = comment,
            status = ClipStatus.TO_WATCH,
            createdAt = System.currentTimeMillis()
        )
        val generatedId = clipDao.insert(clip.toEntity())
        return clip.copy(id = generatedId)
    }

    override suspend fun updateStatus(clipId: Long, status: ClipStatus) {
        clipDao.updateStatus(clipId, status.name)
    }

    override suspend fun updateComment(clipId: Long, comment: String?) = clipDao.updateComment(clipId, comment)

    override suspend fun deleteClip(clipId: Long) {
        val entity = clipDao.getById(clipId) ?: return
        clipDao.delete(entity)
    }
}
