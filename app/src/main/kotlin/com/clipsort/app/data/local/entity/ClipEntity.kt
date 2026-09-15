package com.clipsort.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représentation persistée d'un clip. La contrainte de clé étrangère garantit
 * qu'un clip ne peut pas référencer une catégorie inexistante, et CASCADE
 * supprime les clips orphelins si leur catégorie est supprimée.
 */
@Entity(
    tableName = "clips",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val sourceApp: String,
    val categoryId: Long,
    val comment: String?,
    val status: String,
    val createdAt: Long
)
