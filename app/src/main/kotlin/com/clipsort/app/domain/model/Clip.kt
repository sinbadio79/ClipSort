package com.clipsort.app.domain.model

/**
 * Plateforme source d'un clip partagé, détectée à partir de l'URL.
 */
enum class SourceApp {
    TIKTOK,
    INSTAGRAM,
    YOUTUBE,
    FACEBOOK,
    UNKNOWN
}

/**
 * Statut de visionnage d'un clip.
 */
enum class ClipStatus {
    TO_WATCH,
    WATCHED
}

/**
 * Représente un clip sauvegardé : un lien partagé, catégorisé et éventuellement commenté.
 * Modèle métier pur : aucune dépendance à Room ou à Android.
 */
data class Clip(
    val id: Long = 0L,
    val url: String,
    val sourceApp: SourceApp,
    val categoryId: Long,
    val comment: String?,
    val status: ClipStatus,
    val createdAt: Long
)
