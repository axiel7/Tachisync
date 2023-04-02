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
import com.axiel7.tachisync.utils.FileUtils.areUriPermissionsGranted
import com.axiel7.tachisync.utils.FileUtils.releaseUriPermissions
import com.axiel7.tachisync.utils.SharedPrefsHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilesViewModel: BaseViewModel() {

    var downloadedManga by mutableStateOf(emptyList<Manga>())

    var openIntentForDirectory by mutableStateOf(false)

    fun refresh(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val tachiyomiUri = SharedPrefsHelpers.instance?.getString("tachiyomi_uri", null)
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
                context.releaseUriPermissions(downloadsUri)
                SharedPrefsHelpers.instance?.deleteValue("tachiyomi_uri")
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

    var openTachiyomiDirectoryHelpDialog by mutableStateOf(false)
}