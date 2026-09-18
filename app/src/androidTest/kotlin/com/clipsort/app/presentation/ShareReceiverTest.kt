package com.clipsort.app.presentation

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.clipsort.app.ShareReceiverActivity
import com.clipsort.app.data.local.entity.CategoryEntity
import com.clipsort.app.di.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real activity + Hilt + Room, including the manifest's singleTask delivery. */
@RunWith(AndroidJUnit4::class)
class ShareReceiverTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun seedDatabase() = runBlocking(Dispatchers.IO) {
        val db = DatabaseModule.provideAppDatabase(context)
        try {
            db.clearAllTables()
            db.categoryDao().insert(CategoryEntity(1, "Cuisine", "#417F71", 1))
        } finally { db.close() }
    }

    private fun share(text: String) = Intent(context, ShareReceiverActivity::class.java).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun awaitCategory() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("share-category-1").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun savesExtractedUrlAndNoteThroughRealRepository() {
        ActivityScenario.launch<ShareReceiverActivity>(share("Une recette https://youtu.be/example #cuisine")).use { scenario ->
            awaitCategory()
            compose.onNodeWithTag("shared-url").assertTextEquals("https://youtu.be/example")
            compose.onNodeWithTag("share-category-1").performScrollTo().performClick()
            compose.onNodeWithTag("share-note").performScrollTo().performTextInput("  Dimanche  ")
            compose.onNodeWithTag("share-save").performScrollTo().performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
        runBlocking(Dispatchers.IO) {
            val db = DatabaseModule.provideAppDatabase(context)
            try {
                val clip = db.clipDao().observeAll().first().single()
                assertEquals("https://youtu.be/example", clip.url)
                assertEquals("Dimanche", clip.comment)
                assertEquals(1L, clip.categoryId)
            } finally { db.close() }
        }
    }

    @Test fun secondShareCannotInheritPreviousNoteOrSelection() {
        ActivityScenario.launch<ShareReceiverActivity>(share("https://youtu.be/first")).use { scenario ->
            awaitCategory()
            compose.onNodeWithTag("share-category-1").performScrollTo().performClick()
            compose.onNodeWithTag("share-note").performScrollTo().performTextInput("Ancienne note")
            scenario.onActivity { it.startActivity(share("https://youtu.be/second")) }
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("https://youtu.be/second").fetchSemanticsNodes().isNotEmpty()
            }
            awaitCategory()
            compose.onNodeWithTag("share-category-1").assertIsNotSelected()
            assertEquals("", compose.onNodeWithTag("share-note").fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
            compose.onNodeWithTag("share-save").performScrollTo().assertIsNotEnabled()
        }
    }
}
