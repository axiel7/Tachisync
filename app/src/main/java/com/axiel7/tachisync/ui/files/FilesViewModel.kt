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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel : BaseViewModel<FilesUiState>(), FilesEvent {

    override val mutableUiState = MutableStateFlow(FilesUiState())

    override fun onSelectedManga(index: Int, selected: Boolean) {
        mutableUiState.value.run {
            downloadedManga[index] = downloadedManga[index].copy(isSelected = selected)

            if (selected) selectedMangaIndices.add(index)
            else selectedMangaIndices.remove(index)
        }
    }

    override fun selectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.value.run {
                for (index in downloadedManga.indices) {
                    downloadedManga[index] = downloadedManga[index].copy(isSelected = true)
                }
                selectedMangaIndices.clear()
                selectedMangaIndices.addAll(downloadedManga.indices)
            }
        }
    }

    override fun deselectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableUiState.value.run {
                for (index in downloadedManga.indices) {
                    downloadedManga[index] = downloadedManga[index].copy(isSelected = false)
                }
                selectedMangaIndices.clear()
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