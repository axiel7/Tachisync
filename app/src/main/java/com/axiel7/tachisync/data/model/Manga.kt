package com.axiel7.tachisync.data.model

import androidx.documentfile.provider.DocumentFile

data class Manga(
    val name: String,
    val chapters: Int,
    val file: DocumentFile,
    val isSelected: Boolean = false
)
