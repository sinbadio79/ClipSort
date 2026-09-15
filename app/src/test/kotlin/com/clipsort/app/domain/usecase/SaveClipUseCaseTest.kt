package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.domain.repository.ClipRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveClipUseCaseTest {

    private val clipRepository = mockk<ClipRepository>()
    private val useCase = SaveClipUseCase(clipRepository)

    @Test
    fun `fails when categoryId is invalid`() = runTest {
        val result = useCase(
            url = "https://www.tiktok.com/video/1",
            sourceApp = SourceApp.TIKTOK,
            categoryId = 0L,
            comment = null
        )

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `fails when url is blank`() = runTest {
        val result = useCase(
            url = "   ",
            sourceApp = SourceApp.TIKTOK,
            categoryId = 1L,
            comment = null
        )

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `saves clip and trims comment when input is valid`() = runTest {
        val expectedClip = Clip(
            id = 1L,
            url = "https://www.tiktok.com/video/1",
            sourceApp = SourceApp.TIKTOK,
            categoryId = 42L,
            comment = "recette pâtes",
            status = ClipStatus.TO_WATCH,
            createdAt = 1_700_000_000_000L
        )

        coEvery {
            clipRepository.saveClip(
                url = "https://www.tiktok.com/video/1",
                sourceApp = SourceApp.TIKTOK,
                categoryId = 42L,
                comment = "recette pâtes"
            )
        } returns expectedClip

        val result = useCase(
            url = "https://www.tiktok.com/video/1",
            sourceApp = SourceApp.TIKTOK,
            categoryId = 42L,
            comment = "  recette pâtes  "
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedClip)
    }
}
