package com.clipsort.app.presentation

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.presentation.share.CategorizeContent
import com.clipsort.app.presentation.share.CategorizeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun savingRequiresExplicitCollectionAndKeepsNote() {
        var saved: CategorizeUiState? = null
        compose.setContent {
            var state by remember {
                mutableStateOf(CategorizeUiState(
                    sharedUrl = "https://www.youtube.com/shorts/example1",
                    detectedSource = SourceApp.YOUTUBE, categories = sampleCategories
                ))
            }
            FrenchTheme {
                Surface {
                    CategorizeContent(state,
                        { state = state.copy(selectedCategoryId = it) },
                        { state = state.copy(comment = it) }, {}, {}, {}, {},
                        { saved = state })
                }
            }
        }
        compose.onNodeWithTag("share-save").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("share-category-1").performScrollTo().performClick()
        compose.onNodeWithTag("share-note").performScrollTo().performTextInput("À tester dimanche")
        compose.onNodeWithTag("share-save").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(1L, saved?.selectedCategoryId)
            assertEquals("À tester dimanche", saved?.comment)
        }
    }

    @Test fun shareScreenShowsSourceCollectionsAndNote() {
        compose.setContent {
            FrenchTheme {
                Surface {
                    CategorizeContent(
                        CategorizeUiState(
                            sharedUrl = "https://www.youtube.com/shorts/example1",
                            detectedSource = SourceApp.YOUTUBE, categories = sampleCategories,
                            selectedCategoryId = 1, comment = "Des pâtes au citron en 15 minutes"
                        ), {}, {}, {}, {}, {}, {}, {}
                    )
                }
            }
        }
        compose.onNodeWithTag("share-category-1").assertIsSelected()
        saveScreenCapture("04-share", compose.onRoot().captureToImage().asAndroidBitmap())
    }
}
