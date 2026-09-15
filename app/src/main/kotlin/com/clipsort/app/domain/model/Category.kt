package com.clipsort.app.domain.model

/**
 * Représente une catégorie créée librement par l'utilisateur pour trier ses clips.
 * Modèle métier pur : aucune dépendance à Room ou à Android.
 */
data class Category(
    val id: Long = 0L,
    val name: String,
    val colorHex: String,
    val createdAt: Long
)

/**
 * Catégorie enrichie du nombre de clips qu'elle contient, utilisée pour l'écran bibliothèque.
 */
data class CategoryWithCount(
    val category: Category,
    val clipCount: Int
)
