package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FilterClipsUseCaseTest {
    private val categories = listOf(Category(1, "Cuisine", "#123456", 1))
    private val first = Clip(1, "https://youtu.be/one", SourceApp.YOUTUBE, 1, "Pâtes au citron", ClipStatus.TO_WATCH, 1)
    private val second = first.copy(id = 2, sourceApp = SourceApp.INSTAGRAM, status = ClipStatus.WATCHED, createdAt = 2)
    private val third = first.copy(id = 3, comment = null, categoryId = 99, createdAt = 3)
    private val clips = listOf(first, second, third)
    private val filterClips = FilterClipsUseCase()

    @Test fun `query matches comment and category across terms ignoring case`() {
        assertThat(filterClips(clips, categories, ClipFilter(query = "  CUISINE   citron  "))).containsExactly(second, first).inOrder()
    }

    @Test fun `source status and query are intersected`() {
        val filter = ClipFilter("citron", SourceApp.YOUTUBE, ClipStatus.TO_WATCH)
        assertThat(filterClips(clips, categories, filter)).containsExactly(first)
    }

    @Test fun `resetting filters returns every clip newest first`() {
        assertThat(filterClips(clips, categories, ClipFilter())).containsExactly(third, second, first).inOrder()
    }

    @Test fun `oldest order uses id to resolve identical timestamps`() {
        val sameTime = second.copy(createdAt = 1)
        assertThat(filterClips(listOf(sameTime, first), categories, ClipFilter(order = ClipOrder.OLDEST)))
            .containsExactly(first, sameTime).inOrder()
    }

    @Test fun `missing category and comment remain searchable by url`() {
        assertThat(filterClips(listOf(third), categories, ClipFilter(query = "youtu.be"))).containsExactly(third)
    }

    @Test fun `all search terms must match`() {
        assertThat(filterClips(clips, categories, ClipFilter(query = "citron voyage"))).isEmpty()
    }
}
