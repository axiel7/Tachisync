package com.axiel7.tachisync.ui.files

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.releaseUriPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilesViewModel: BaseViewModel() {

    var downloadedManga by mutableStateOf(emptyList<Manga>())

    var tachiyomiDownloadsUri: Uri? = null
    var openIntentForDirectory by mutableStateOf(false)

    fun getTachiyomiDirectory() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            //TODO: check tachiyomi forks dirs
            val tachiyomiDirs = Environment.getExternalStorageDirectory()
                .listFiles { _, name -> name == "Tachiyomi" }

            if (tachiyomiDirs?.isNotEmpty() == true) {
                val downloadsDir = tachiyomiDirs[0].listFiles { _, name -> name == "downloads" }

                if (downloadsDir?.isNotEmpty() == true) {
                    tachiyomiDownloadsUri = downloadsDir[0].toUri()
                    openIntentForDirectory = true

                } else setErrorMessage("Download some content first on Tachiyomi")
            } else setErrorMessage("Tachiyomi directory not found")

            isLoading = false
        }
    }

    fun readDownloadsDir(downloadsUri: Uri, context: Context) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val tempContent = mutableListOf<Manga>()
            val sourcesDir = DocumentFile.fromTreeUri(context, downloadsUri)
            if (sourcesDir == null || !sourcesDir.exists()) {
                context.releaseUriPermissions(downloadsUri)
            } else {
                sourcesDir.listFiles().forEach { sourceFile ->
                    sourceFile.listFiles().forEach { series ->
                        val chaptersDownloaded = series.listFiles().size
                        if (chaptersDownloaded > 0)
                            tempContent.add(Manga(series.name ?: "Unknown", chaptersDownloaded, series))
                    }
                }
                downloadedManga = tempContent
            }

            isLoading = false
        }
    }
}