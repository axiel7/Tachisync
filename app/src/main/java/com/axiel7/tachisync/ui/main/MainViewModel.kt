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

    var tachiyomiUri by mutableStateOf<Uri?>(null)
    var externalSyncUri by mutableStateOf<Uri?>(null)

    var isSyncing by mutableStateOf(false)
    var progress by mutableStateOf(0f)

    fun syncContents(context: Context, contents: List<Manga>, selected: List<Int>) {
        isSyncing = true
        viewModelScope.launch(Dispatchers.IO) {
            if (selected.isEmpty()) setErrorMessage("No content selected")
            else if (externalSyncUri == null) setErrorMessage("No external directory selected")
            else {
                try {
                    val destDir = DocumentFile.fromTreeUri(context, externalSyncUri!!)
                    if (destDir?.isDirectory == false) {
                        setErrorMessage("Invalid external directory")
                    } else {
                        val selectedContent = contents.filterIndexed { index, _ -> selected.contains(index) }
                        val selectedCount = selectedContent.size.toFloat()
                        val files = selectedContent.map { it.file }
                        files.forEachIndexed { index, file ->
                            context.syncDirectory(sourceDir = file, destRootDir = destDir!!)
                            progress = (index + 1).div(selectedCount)
                        }
                    }
                } catch (e: Exception) {
                    setErrorMessage(e.message ?: "Error syncing")
                    isSyncing = false
                    return@launch
                }
            }
            isSyncing = false
            setErrorMessage("Sync completed")
        }
    }
}