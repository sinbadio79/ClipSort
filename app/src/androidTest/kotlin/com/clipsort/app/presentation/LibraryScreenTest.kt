package com.clipsort.app.presentation

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.clipsort.app.domain.model.*
import com.clipsort.app.presentation.library.LibraryContent
import com.clipsort.app.presentation.library.LibraryUiState
import com.clipsort.app.presentation.theme.ClipSortTheme
import java.io.File
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeOffersVisibleCollectionAndNavigationActions() {
        var opened = 0L
        var added = false
        var browsed = false
        compose.setContent {
            FrenchTheme { LibraryContent(sampleLibrary, { opened = it }, { browsed = true }, { added = true }, {}) }
        }
        compose.onNodeWithTag("nav-clips").performClick()
        compose.runOnIdle { assertEquals(true, browsed) }
        compose.onNodeWithTag("add-collection").performClick()
        compose.runOnIdle { assertEquals(true, added) }
        captureScreen("01-home")
        compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("collection-1"))
        compose.onNodeWithTag("collection-1").performClick()
        compose.runOnIdle { assertEquals(1L, opened) }
    }

    @Test fun emptyHomeInvitesCreationInsteadOfDisplayingFakeData() {
        var added = false
        compose.setContent {
            FrenchTheme { LibraryContent(LibraryUiState(isLoading = false), {}, {}, { added = true }, {}) }
        }
        compose.onNodeWithText("Créer une collection").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(true, added) }
        compose.onNodeWithTag("browse-clips").assertDoesNotExist()
    }

    @Test fun homeSupportsDarkMode() {
        compose.setContent { FrenchTheme(dark = true) { LibraryContent(sampleLibrary, {}, {}, {}, {}) } }
        compose.onNodeWithTag("nav-home").assertIsSelected()
        captureScreen("01-home-dark")
    }

    private fun captureScreen(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = instrumentation.targetContext.getExternalFilesDir("screenshots")!!
        directory.mkdirs()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}

@Composable
fun FrenchTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale.FRENCH) }
    CompositionLocalProvider(LocalContext provides context.createConfigurationContext(configuration), LocalConfiguration provides configuration) {
        ClipSortTheme(darkTheme = dark, content = content)
    }
}

// Demonstration fixtures live only in tests; the application always shows Room data.
val sampleCategories = listOf(
    Category(1, "À cuisiner", "#CF784F", 1),
    Category(2, "Escapades", "#417F71", 2),
    Category(3, "Petites idées", "#9274AC", 3),
    Category(4, "Bouger un peu", "#567AA4", 4)
)
val sampleClips = listOf(
    Clip(1, "https://www.youtube.com/shorts/example1", SourceApp.YOUTUBE, 1, "Des pâtes au citron en 15 minutes", ClipStatus.TO_WATCH, 1789600000000),
    Clip(2, "https://www.instagram.com/reel/example2/", SourceApp.INSTAGRAM, 2, "Un week-end au bord de l’océan", ClipStatus.TO_WATCH, 1789500000000),
    Clip(3, "https://www.tiktok.com/@example/video/3", SourceApp.TIKTOK, 1, "Le secret d’un bon café glacé", ClipStatus.WATCHED, 1789400000000),
    Clip(4, "https://youtu.be/example4", SourceApp.YOUTUBE, 3, "Une étagère qui change tout", ClipStatus.TO_WATCH, 1789300000000)
)
val sampleLibrary = LibraryUiState(
    categories = sampleCategories.map { category -> CategoryWithCount(category, sampleClips.count { it.categoryId == category.id }) },
    clips = sampleClips,
    isLoading = false
)
