package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.SourceApp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DetectSourceAppUseCaseTest {

    private val useCase = DetectSourceAppUseCase()

    @Test
    fun `detects TikTok link`() {
        val result = useCase("Regarde ça https://www.tiktok.com/@user/video/123")
        assertThat(result).isEqualTo(SourceApp.TIKTOK)
    }

    @Test
    fun `detects Instagram link`() {
        val result = useCase("https://www.instagram.com/reel/abc123/")
        assertThat(result).isEqualTo(SourceApp.INSTAGRAM)
    }

    @Test
    fun `detects YouTube short link`() {
        val result = useCase("https://youtu.be/xyz")
        assertThat(result).isEqualTo(SourceApp.YOUTUBE)
    }

    @Test
    fun `returns UNKNOWN when no url is present`() {
        val result = useCase("juste du texte sans lien")
        assertThat(result).isEqualTo(SourceApp.UNKNOWN)
    }
}
