package com.clipsort.app.presentation

import androidx.lifecycle.ViewModelStore
import com.clipsort.app.domain.model.*
import com.clipsort.app.domain.repository.*
import com.clipsort.app.domain.usecase.*
import com.clipsort.app.presentation.share.CategorizeViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategorizeViewModelTest {
    private val clips = mockk<ClipRepository>()
    private val categories = mockk<CategoryRepository>()
    private val store = ViewModelStore()

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { categories.observeCategories() } returns MutableStateFlow(emptyList())
    }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }
    private fun model() = CategorizeViewModel(
        DetectSourceAppUseCase(), SaveClipUseCase(clips), CreateCategoryUseCase(categories), categories
    ).also { store.put("share", it) }

    @Test fun `rapid save clicks create exactly one record`() = runTest {
        val clip = Clip(1, "https://youtu.be/one", SourceApp.YOUTUBE, 1, null, ClipStatus.TO_WATCH, 1)
        coEvery { clips.saveClip(any(), any(), any(), any()) } returns clip
        val model = model()
        model.onSharedTextReceived(clip.url)
        model.onCategorySelected(1)
        model.onSaveClicked()
        model.onSaveClicked()
        assertThat(model.uiState.value.isSaving).isTrue()
        advanceUntilIdle()
        model.onSaveClicked()
        coVerify(exactly = 1) { clips.saveClip(any(), any(), any(), any()) }
        assertThat(model.uiState.value.isSaved).isTrue()
    }

    @Test fun `rapid category creation creates exactly one category`() = runTest {
        coEvery { categories.createCategory(any(), any()) } returns Category(1, "Cuisine", "#417F71", 1)
        val model = model()
        model.onCreateCategoryClicked()
        model.onNewCategoryNameChanged("Cuisine")
        model.onConfirmCreateCategory()
        model.onConfirmCreateCategory()
        advanceUntilIdle()
        coVerify(exactly = 1) { categories.createCategory(any(), any()) }
        assertThat(model.uiState.value.selectedCategoryId).isEqualTo(1L)
        assertThat(model.uiState.value.isSubmittingCategory).isFalse()
    }
}
