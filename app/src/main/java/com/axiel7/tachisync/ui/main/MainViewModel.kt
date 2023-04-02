package com.axiel7.tachisync.ui.main

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.syncDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel: BaseViewModel() {

    private var selectedManga = mutableListOf<Manga>()
    var selectedCount by mutableStateOf(0)

    fun onSelectedManga(manga: Manga, selected: Boolean) {
        if (selected) {
            selectedManga.add(manga)
        } else {
            selectedManga.remove(manga)
        }
        selectedCount = selectedManga.size
    }

    var tachiyomiUri by mutableStateOf<Uri?>(null)
    var externalSyncUri by mutableStateOf<Uri?>(null)

    var isSyncing by mutableStateOf(false)
    var progress by mutableStateOf(0f)

    fun syncContents(context: Context) {
        isSyncing = true
        progress = 1f
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedManga.isEmpty()) setErrorMessage("No content selected")
            else if (externalSyncUri == null) setErrorMessage("No external directory selected")
            else {
                try {
                    val destDir = DocumentFile.fromTreeUri(context, externalSyncUri!!)
                    if (destDir?.isDirectory == false) {
                        setErrorMessage("Invalid external directory")
                    } else {
                        val files = selectedManga.map { it.file }
                        files.forEachIndexed { index, file ->
                            context.syncDirectory(sourceDir = file, destRootDir = destDir!!)
                            progress = (index / selectedCount).toFloat()
                        }
                    }
                } catch (e: Exception) {
                    setErrorMessage(e.message ?: "Error syncing")
                }
            }
            isSyncing = false
            setErrorMessage("Sync completed")
        }
    }
}