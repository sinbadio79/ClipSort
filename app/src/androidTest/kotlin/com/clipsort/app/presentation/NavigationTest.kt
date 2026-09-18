package com.clipsort.app.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.clipsort.app.MainActivity
import com.clipsort.app.data.local.entity.CategoryEntity
import com.clipsort.app.data.local.entity.ClipEntity
import com.clipsort.app.di.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Before fun seedDatabase(): Unit = runBlocking(Dispatchers.IO) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = DatabaseModule.provideAppDatabase(context)
        try {
            db.clearAllTables()
            db.categoryDao().insert(CategoryEntity(1, "Cuisine", "#417F71", 1))
            db.categoryDao().insert(CategoryEntity(2, "Voyages", "#417F71", 2))
            db.clipDao().insert(ClipEntity(1, "https://youtu.be/one", "YOUTUBE", 1, "Citron", "TO_WATCH", 2))
            db.clipDao().insert(ClipEntity(2, "https://youtu.be/two", "YOUTUBE", 2, "Océan", "TO_WATCH", 1))
            Unit
        } finally { db.close() }
    }

    private fun awaitTag(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun collectionAllClipsAndHomeUseRealNavigationAndRepositories() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitTag("collection-1")
            compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("collection-1"))
            compose.onNodeWithTag("collection-1").performClick()
            awaitTag("clip-1")
            compose.onNodeWithTag("clip-2").assertDoesNotExist()
            compose.onNodeWithTag("nav-clips").performClick()
            awaitTag("clip-2")
            compose.onNodeWithTag("nav-home").performClick()
            awaitTag("home-list")
            compose.onNodeWithTag("nav-home").assertIsSelected()
            compose.onNodeWithTag("nav-clips").performClick()
            awaitTag("clip-1")
            compose.onNodeWithTag("clip-search").performTextInput("Citron")
            compose.onNodeWithTag("clip-2").assertDoesNotExist()
            scenario.recreate()
            awaitTag("clip-1")
            compose.onNodeWithTag("clip-search").assertTextContains("Citron")
            compose.onNodeWithTag("clip-2").assertDoesNotExist()
        }
    }
}
