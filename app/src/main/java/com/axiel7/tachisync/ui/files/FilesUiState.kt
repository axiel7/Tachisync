package com.axiel7.tachisync.ui.files

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.UiState

@Stable
data class FilesUiState(
    val downloadedManga: SnapshotStateList<Manga> = mutableStateListOf(),
    val selectedMangaIndices: SnapshotStateList<Int> = mutableStateListOf(),
    val openIntentForDirectory: Boolean = false,
    val openTachiyomiDirectoryHelpDialog: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
