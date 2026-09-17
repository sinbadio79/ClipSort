package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import javax.inject.Inject

enum class ClipOrder { NEWEST, OLDEST }

data class ClipFilter(
    val query: String = "",
    val source: SourceApp? = null,
    val status: ClipStatus? = null,
    val order: ClipOrder = ClipOrder.NEWEST
)

/** Local reactive filtering; this is not a database full-text index. */
class FilterClipsUseCase @Inject constructor() {
    operator fun invoke(clips: List<Clip>, categories: List<Category>, filter: ClipFilter): List<Clip> {
        val categoryNames = categories.associate { it.id to it.name }
        val terms = filter.query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val matching = clips.filter { clip ->
            (filter.source == null || clip.sourceApp == filter.source) &&
                (filter.status == null || clip.status == filter.status) &&
                terms.all { term ->
                    listOf(clip.comment.orEmpty(), clip.url, categoryNames[clip.categoryId].orEmpty(), clip.sourceApp.name)
                        .any { it.contains(term, ignoreCase = true) }
                }
        }
        val comparator = compareBy<Clip> { it.createdAt }.thenBy { it.id }
        return matching.sortedWith(if (filter.order == ClipOrder.NEWEST) comparator.reversed() else comparator)
    }
}
