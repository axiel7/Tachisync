package com.axiel7.tachisync.ui.files

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

class FilesViewModel : BaseViewModel() {

    val downloadedManga = mutableStateListOf<Manga>()

    var selectedManga = mutableListOf<Int>()
    var selectedCount by mutableIntStateOf(0)

    fun onSelectedManga(index: Int, selected: Boolean) {
        downloadedManga[index] = downloadedManga[index].copy(isSelected = selected)
        if (selected) {
            selectedManga.add(index)
        } else {
            selectedManga.remove(index)
        }
        selectedCount = selectedManga.size
    }

    fun selectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedManga.clear()
            for (index in downloadedManga.indices) {
                downloadedManga[index] = downloadedManga[index].copy(isSelected = true)
                selectedManga.add(index)
            }
            selectedCount = downloadedManga.size
        }
    }

    fun deselectAllManga() {
        viewModelScope.launch(Dispatchers.IO) {
            for (index in downloadedManga.indices) {
                downloadedManga[index] = downloadedManga[index].copy(isSelected = false)
            }
            selectedManga.clear()
            selectedCount = 0
        }
    }

    var openIntentForDirectory by mutableStateOf(false)

    fun refresh(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            deselectAllManga()
            val tachiyomiUri = PreferencesRepository.get(TACHIYOMI_URI_KEY)
            if (tachiyomiUri.isNullOrEmpty() || !context.areUriPermissionsGranted(tachiyomiUri)) {
                openTachiyomiDirectoryHelpDialog = true
            } else {
                readDownloadsDir(Uri.parse(tachiyomiUri), context)
            }
        }
    }

    fun readDownloadsDir(downloadsUri: Uri, context: Context) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
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
                downloadedManga.clear()
                downloadedManga.addAll(tempContent)
            }

            isLoading = false
        }
    }

    var openTachiyomiDirectoryHelpDialog by mutableStateOf(false)
}