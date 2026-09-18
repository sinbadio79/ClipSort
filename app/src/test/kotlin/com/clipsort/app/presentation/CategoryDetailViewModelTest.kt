package com.clipsort.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.clipsort.app.domain.model.*
import com.clipsort.app.domain.repository.CategoryRepository
import com.clipsort.app.domain.repository.ClipRepository
import com.clipsort.app.domain.usecase.FilterClipsUseCase
import com.clipsort.app.presentation.categorydetail.CategoryDetailViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModelTest {
    private val repository = mockk<ClipRepository>()
    private val categories = mockk<CategoryRepository>()
    private val clip = Clip(1, "https://youtu.be/one", SourceApp.YOUTUBE, 1, "Citron", ClipStatus.TO_WATCH, 1)
    private val clipFlow = MutableStateFlow(listOf(clip))
    private val store = ViewModelStore()

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.observeAllClips() } returns clipFlow
        every { categories.observeCategories() } returns MutableStateFlow(listOf(Category(1, "Cuisine", "#123456", 1)))
    }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }
    private fun model(handle: SavedStateHandle = SavedStateHandle()): CategoryDetailViewModel =
        CategoryDetailViewModel(handle, repository, categories, FilterClipsUseCase()).also { store.put("model", it) }

    @Test fun `query is restored and repository updates change results`() = runTest {
        val handle = SavedStateHandle(mapOf("query" to "citron"))
        val model = model(handle)
        assertThat(model.uiState.value.visibleClips).containsExactly(clip)
        clipFlow.value = listOf(clip.copy(comment = "Chocolat"))
        advanceUntilIdle()
        assertThat(model.uiState.value.visibleClips).isEmpty()
        model.onQueryChanged("chocolat")
        assertThat(handle.get<String>("query")).isEqualTo("chocolat")
        assertThat(model.uiState.value.visibleClips).hasSize(1)
    }

    @Test fun `deletion requires confirmation and preserves dialog after failure`() = runTest {
        coEvery { repository.deleteClip(clip.id) } throws IllegalStateException("unavailable")
        val model = model()
        model.onDeleteRequested(clip)
        coVerify(exactly = 0) { repository.deleteClip(any()) }
        model.onConfirmDelete()
        advanceUntilIdle()
        assertThat(model.uiState.value.pendingDeletion).isEqualTo(clip)
        assertThat(model.uiState.value.errorMessage).isNotNull()
        assertThat(model.uiState.value.isWorking).isFalse()
    }

    @Test fun `editing a note trims text before persisting`() = runTest {
        coEvery { repository.updateComment(clip.id, "À essayer") } returns Unit
        val model = model()
        model.onEditRequested(clip)
        model.onNoteChanged("  À essayer  ")
        model.onConfirmEdit()
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.updateComment(clip.id, "À essayer") }
        assertThat(model.uiState.value.pendingEdit).isNull()
    }
}
