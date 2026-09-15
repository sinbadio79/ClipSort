package com.clipsort.app.domain.usecase

import com.clipsort.app.domain.model.SourceApp
import javax.inject.Inject

/**
 * Détermine la plateforme d'origine d'un lien partagé à partir de son domaine.
 * Isolé en use case pour rester testable unitairement, sans dépendance Android.
 */
class DetectSourceAppUseCase @Inject constructor() {

    operator fun invoke(sharedText: String): SourceApp {
        val url = extractUrl(sharedText) ?: return SourceApp.UNKNOWN

        return when {
            url.contains("tiktok.com") -> SourceApp.TIKTOK
            url.contains("instagram.com") -> SourceApp.INSTAGRAM
            url.contains("youtube.com") || url.contains("youtu.be") -> SourceApp.YOUTUBE
            url.contains("facebook.com") || url.contains("fb.watch") -> SourceApp.FACEBOOK
            else -> SourceApp.UNKNOWN
        }
    }

    private fun extractUrl(text: String): String? {
        val regex = Regex("https?://\\S+")
        return regex.find(text)?.value
    }
}
