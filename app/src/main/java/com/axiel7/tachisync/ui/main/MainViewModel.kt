package com.axiel7.tachisync.ui.main

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.App
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.EXTERNAL_URI_KEY
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.TACHIYOMI_URI_KEY
import com.axiel7.tachisync.data.datastore.PreferencesRepository
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.syncDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    val externalSyncUri = App.dataStore.data
        .map { it[EXTERNAL_URI_KEY]?.let { uri -> Uri.parse(uri) } }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onExternalUriChanged(value: String) = viewModelScope.launch {
        PreferencesRepository.set(EXTERNAL_URI_KEY, value)
    }

    fun onTachiyomiUriChanged(value: String) = viewModelScope.launch {
        PreferencesRepository.set(TACHIYOMI_URI_KEY, value)
    }

    var isSyncing by mutableStateOf(false)
    val progress = mutableFloatStateOf(0f)
    private var currentFileCount = mutableIntStateOf(0)

    fun syncContents(context: Context, contents: List<Manga>, selected: List<Int>) {
        isSyncing = true
        viewModelScope.launch(Dispatchers.IO) {
            progress.floatValue = 0f
            val externalUri = externalSyncUri.value
            if (selected.isEmpty()) setErrorMessage("No content selected")
            else if (externalUri == null) setErrorMessage("No external directory selected")
            else {
                try {
                    val destDir = DocumentFile.fromTreeUri(context, externalUri)
                    if (destDir?.isDirectory == false) {
                        setErrorMessage("Invalid external directory")
                    } else {
                        val selectedContent =
                            contents.filterIndexed { index, _ -> selected.contains(index) }
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