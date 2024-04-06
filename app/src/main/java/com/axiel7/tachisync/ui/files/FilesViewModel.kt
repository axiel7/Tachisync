package com.axiel7.tachisync.ui.files

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.App
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.TACHIYOMI_URI_KEY
import com.axiel7.tachisync.data.datastore.PreferencesRepository
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.areUriPermissionsGranted
import com.axiel7.tachisync.utils.FileUtils.releaseUriPermissions
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel : BaseViewModel<FilesUiState>(), FilesEvent {

    override val mutableUiState = MutableStateFlow(FilesUiState())

    override fun onSelectedManga(index: Int, selected: Boolean) {
        mutableUiState.update {
            it.downloadedManga[index] = it.downloadedManga[index].copy(isSelected = selected)

            val selectedIndices = if (selected) it.selectedMangaIndices.plus(index)
            else it.selectedMangaIndices.minus(index)
            it.copy(
                selectedMangaIndices = selectedIndices.toImmutableList()
            )
        }
    }

    override fun selectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update {
                for (index in it.downloadedManga.indices) {
                    it.downloadedManga[index] = it.downloadedManga[index].copy(isSelected = true)
                }
                it.copy(selectedMangaIndices = it.downloadedManga.indices.toImmutableList())
            }
        }
    }

    override fun deselectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.update {
                for (index in it.downloadedManga.indices) {
                    it.downloadedManga[index] = it.downloadedManga[index].copy(isSelected = false)
                }
                it.copy(selectedMangaIndices = persistentListOf())
            }
        }
    }

    override fun refresh(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            deselectAllManga()
            val tachiyomiUri = PreferencesRepository.get(TACHIYOMI_URI_KEY)
            if (tachiyomiUri.isNullOrEmpty() || !context.areUriPermissionsGranted(tachiyomiUri)) {
                setOpenTachiyomiDirectoryHelpDialog(true)
            } else {
                readDownloadsDir(Uri.parse(tachiyomiUri), context)
            }
        }
    }

    override fun readDownloadsDir(downloadsUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val tempContent = mutableListOf<Manga>()
            val sourcesDir = DocumentFile.fromTreeUri(context, downloadsUri)
            if (sourcesDir == null || !sourcesDir.exists()) {
                App.applicationContext.releaseUriPermissions(downloadsUri)
                PreferencesRepository.remove(TACHIYOMI_URI_KEY)
            } else {
                sourcesDir.listFiles().forEach { sourceFile ->
                    sourceFile.listFiles().forEach { series ->
                        val chaptersDownloaded = series.listFiles().size
                        if (chaptersDownloaded > 0)
                            tempContent.add(
                                Manga(
                                    name = series.name ?: "Unknown",
                                    chapters = chaptersDownloaded,
                                    file = series
                                )
                            )
                    }
                }
                mutableUiState.value.run {
                    downloadedManga.clear()
                    downloadedManga.addAll(tempContent)
                }
            }

            setLoading(false)
        }
    }

    override fun setOpenIntentForDirectory(value: Boolean) {
        mutableUiState.update { it.copy(openIntentForDirectory = value) }
    }

    override fun setOpenTachiyomiDirectoryHelpDialog(value: Boolean) {
        mutableUiState.update { it.copy(openTachiyomiDirectoryHelpDialog = value) }
    }
}