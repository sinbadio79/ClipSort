package com.clipsort.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représentation persistée d'une catégorie. Reste privée à la couche data :
 * ni le domain ni la presentation ne manipulent directement cette classe.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorHex: String,
    val createdAt: Long
)
