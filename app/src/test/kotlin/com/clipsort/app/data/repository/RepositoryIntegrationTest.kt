package com.clipsort.app.data.repository

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.clipsort.app.data.local.AppDatabase
import com.clipsort.app.di.DatabaseModule
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Real Room/SQLite and repositories, using the same database builder as production. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class RepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var categories: CategoryRepositoryImpl
    private lateinit var clips: ClipRepositoryImpl

    @Before
    fun setUp() {
        RuntimeEnvironment.getApplication().deleteDatabase(AppDatabase.DATABASE_NAME)
        openDatabase()
    }

    private fun openDatabase() {
        database = DatabaseModule.provideAppDatabase(RuntimeEnvironment.getApplication())
        categories = CategoryRepositoryImpl(database.categoryDao())
        clips = ClipRepositoryImpl(database.clipDao())
    }

    @After
    fun tearDown() {
        database.close()
        RuntimeEnvironment.getApplication().deleteDatabase(AppDatabase.DATABASE_NAME)
    }

    @Test
    fun `all clips combines categories and reflects status changes and deletion`() = runTest {
        val first = categories.createCategory("Cuisine", "#378ADD")
        val second = categories.createCategory("Sport", "#639922")
        val one = clips.saveClip("https://youtu.be/one", SourceApp.YOUTUBE, first.id, null)
        val two = clips.saveClip("https://youtu.be/two", SourceApp.YOUTUBE, second.id, null)
        assertThat(clips.observeAllClips().first()).containsExactly(one, two)
        clips.updateStatus(one.id, ClipStatus.WATCHED)
        clips.deleteClip(two.id)
        assertThat(clips.observeAllClips().first()).containsExactly(one.copy(status = ClipStatus.WATCHED))
    }

    @Test
    fun `library survives closing and reopening the database`() = runTest {
        val category = categories.createCategory("Cuisine", "#378ADD")
        val clip = clips.saveClip("https://youtu.be/example", SourceApp.YOUTUBE, category.id, "Pâtes")
        clips.updateStatus(clip.id, ClipStatus.WATCHED)
        categories.renameCategory(category.id, "Recettes")
        database.close()
        openDatabase()

        assertThat(categories.getCategoryById(category.id)?.name).isEqualTo("Recettes")
        assertThat(clips.observeClipsByCategory(category.id).first())
            .containsExactly(clip.copy(status = ClipStatus.WATCHED))
        assertThat(categories.observeCategoriesWithCount().first().single().clipCount).isEqualTo(1)
    }

    @Test
    fun `deleting one category removes only its clips`() = runTest {
        val first = categories.createCategory("Cuisine", "#378ADD")
        val second = categories.createCategory("Sport", "#639922")
        clips.saveClip("https://youtu.be/one", SourceApp.YOUTUBE, first.id, null)
        val retained = clips.saveClip("https://youtu.be/two", SourceApp.YOUTUBE, second.id, null)

        categories.deleteCategory(first.id)

        assertThat(clips.observeClipsByCategory(first.id).first()).isEmpty()
        assertThat(clips.observeClipsByCategory(second.id).first()).containsExactly(retained)
        assertThat(categories.observeCategoriesWithCount().first().single().category.id).isEqualTo(second.id)
    }

    @Test
    fun `unsupported database version fails without deleting stored data`() = runTest {
        val category = categories.createCategory("À conserver", "#378ADD")
        database.close()
        val path = RuntimeEnvironment.getApplication().getDatabasePath(AppDatabase.DATABASE_NAME)
        SQLiteDatabase.openDatabase(path.path, null, SQLiteDatabase.OPEN_READWRITE).use {
            it.version = 2
        }
        openDatabase()

        val failure = runCatching { categories.getCategoryById(category.id) }.exceptionOrNull()
        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(failure?.message).contains("migration")
        database.close()
        SQLiteDatabase.openDatabase(path.path, null, SQLiteDatabase.OPEN_READONLY).use { raw ->
            raw.rawQuery("SELECT name FROM categories WHERE id = ?", arrayOf(category.id.toString())).use {
                assertThat(it.moveToFirst()).isTrue()
                assertThat(it.getString(0)).isEqualTo("À conserver")
            }
        }
    }
}
