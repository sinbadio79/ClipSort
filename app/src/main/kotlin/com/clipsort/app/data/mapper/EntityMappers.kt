package com.clipsort.app.data.mapper

import com.clipsort.app.data.local.dao.CategoryWithCountRow
import com.clipsort.app.data.local.entity.CategoryEntity
import com.clipsort.app.data.local.entity.ClipEntity
import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp

/**
 * Point de conversion unique entre les entités Room (couche data) et les modèles
 * métier (couche domain). Aucune autre classe ne doit connaître les deux représentations.
 */

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt
)

fun CategoryWithCountRow.toDomain(): CategoryWithCount = CategoryWithCount(
    category = Category(id = id, name = name, colorHex = colorHex, createdAt = createdAt),
    clipCount = clipCount
)

fun ClipEntity.toDomain(): Clip = Clip(
    id = id,
    url = url,
    sourceApp = runCatching { SourceApp.valueOf(sourceApp) }.getOrDefault(SourceApp.UNKNOWN),
    categoryId = categoryId,
    comment = comment,
    status = runCatching { ClipStatus.valueOf(status) }.getOrDefault(ClipStatus.TO_WATCH),
    createdAt = createdAt
)

fun Clip.toEntity(): ClipEntity = ClipEntity(
    id = id,
    url = url,
    sourceApp = sourceApp.name,
    categoryId = categoryId,
    comment = comment,
    status = status.name,
    createdAt = createdAt
)
