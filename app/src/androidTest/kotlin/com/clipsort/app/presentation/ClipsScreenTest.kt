package com.clipsort.app.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.usecase.ClipFilter
import com.clipsort.app.domain.usecase.FilterClipsUseCase
import com.clipsort.app.presentation.categorydetail.CategoryDetailContent
import com.clipsort.app.presentation.categorydetail.CategoryDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun searchAndStatusIntersectAndCanBeReset() {
        compose.setContent {
            var filter by remember { mutableStateOf(ClipFilter()) }
            FrenchTheme {
                CategoryDetailContent(state(filter), {}, {}, { filter = it }, {}, {}, {}, {}, {})
            }
        }
        compose.waitForIdle()
        saveScreenCapture("02-all-clips", compose.onRoot().captureToImage().asAndroidBitmap())
        compose.onNodeWithTag("clip-search").performTextInput("citron")
        compose.onNodeWithTag("clip-1").assertExists()
        compose.onNodeWithTag("clip-2").assertDoesNotExist()
        compose.onNodeWithTag("status-watched").performClick()
        compose.onNodeWithTag("clip-1").assertDoesNotExist()
        compose.onNodeWithTag("reset-filters").performScrollTo().performClick()
        assertEquals("", compose.onNodeWithTag("clip-search").fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        compose.onNodeWithTag("status-all").assertIsSelected()
        compose.onNodeWithTag("clip-1").assertExists()
    }

    @Test fun readingWatchingSharingAndEditingAreSeparateActions() {
        var opened = 0L
        var watched = 0L
        var shared = 0L
        var edited = 0L
        compose.setContent {
            FrenchTheme {
                CategoryDetailContent(state(), {}, {}, {}, { opened = it.id }, { watched = it.id }, { shared = it.id }, { edited = it.id }, {})
            }
        }
        compose.onNodeWithTag("clip-1").performClick()
        compose.runOnIdle { assertEquals(1L, opened); assertEquals(0L, watched) }
        compose.onNodeWithTag("toggle-1").performClick()
        compose.runOnIdle { assertEquals(1L, watched) }
        compose.onNodeWithTag("share-1").performClick()
        compose.runOnIdle { assertEquals(1L, shared) }
        compose.onNodeWithTag("options-1").performClick()
        compose.onNodeWithTag("edit-clip-1").performClick()
        compose.runOnIdle { assertEquals(1L, edited) }
    }

    @Test fun collectionShowsItsOwnClipsAndWatchedStatus() {
        val clips = sampleClips.filter { it.categoryId == 1L }
        compose.setContent {
            FrenchTheme {
                CategoryDetailContent(state().copy(isCollection = true, categoryName = "À cuisiner", clips = clips, visibleClips = clips), {}, {}, {}, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithTag("clip-2").assertDoesNotExist()
        compose.waitForIdle()
        saveScreenCapture("03-collection", compose.onRoot().captureToImage().asAndroidBitmap())
    }

    private fun state(filter: ClipFilter = ClipFilter()) = CategoryDetailUiState(
        categories = sampleCategories, clips = sampleClips,
        visibleClips = FilterClipsUseCase()(sampleClips, sampleCategories, filter), filter = filter, isLoading = false
    )
}
