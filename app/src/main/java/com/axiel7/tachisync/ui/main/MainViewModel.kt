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
    var progress = mutableStateOf(0f)
    var currentFileCount = mutableStateOf(0)

    fun syncContents(context: Context, contents: List<Manga>, selected: List<Int>) {
        isSyncing = true
        viewModelScope.launch(Dispatchers.IO) {
            progress.value = 0f
            if (selected.isEmpty()) setErrorMessage("No content selected")
            else if (externalSyncUri == null) setErrorMessage("No external directory selected")
            else {
                try {
                    val destDir = DocumentFile.fromTreeUri(context, externalSyncUri!!)
                    if (destDir?.isDirectory == false) {
                        setErrorMessage("Invalid external directory")
                    } else {
                        val selectedContent = contents.filterIndexed { index, _ -> selected.contains(index) }
                        val files = selectedContent.map { it.file }
                        val total = selectedContent.sumOf { it.chapters }.toFloat()
                        files.forEach { file ->
                            context.syncDirectory(
                                sourceDir = file,
                                destRootDir = destDir!!,
                                progress = progress,
                                currentFileCount = currentFileCount,
                                total = total
                            )
                        }
                    }
                } catch (e: Exception) {
                    setErrorMessage(e.message ?: "Error syncing")
                    isSyncing = false
                    return@launch
                }
                setErrorMessage("Sync completed")
            }
            isSyncing = false
        }
    }
}