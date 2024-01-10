package com.axiel7.tachisync.ui.main

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel<MainUiState>(), MainEvent {

    override val mutableUiState = MutableStateFlow(MainUiState())

    override fun onExternalUriChanged(value: String) {
        viewModelScope.launch {
            PreferencesRepository.set(EXTERNAL_URI_KEY, value)
        }
    }

    override fun onTachiyomiUriChanged(value: String) {
        viewModelScope.launch {
            PreferencesRepository.set(TACHIYOMI_URI_KEY, value)
        }
    }

    override fun syncContents(context: Context, contents: List<Manga>, selected: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            val externalUri = uiState.value.externalSyncUri
            if (selected.isEmpty()) {
                showMessage("No content selected")
            } else if (externalUri == null) {
                showMessage("No external directory selected")
            } else {
                mutableUiState.update { it.copy(isLoading = true, syncProgress = 0f) }
                try {
                    val destDir = DocumentFile.fromTreeUri(context, externalUri)
                    if (destDir == null || !destDir.isDirectory) {
                        showMessage("Invalid external directory")
                    } else {
                        val currentFileCount = mutableIntStateOf(0)
                        val selectedContent =
                            contents.filterIndexed { index, _ -> selected.contains(index) }
                        val files = selectedContent.map { it.file }
                        val total = selectedContent.sumOf { it.chapters }.toFloat()
                        files.forEach { file ->
                            context.syncDirectory(
                                sourceDir = file,
                                destRootDir = destDir,
                                progress = uiState.value.syncProgress,
                                updateProgress = this@MainViewModel::updateProgress,
                                currentFileCount = currentFileCount,
                                total = total
                            )
                        }
                    }
                } catch (e: Exception) {
                    mutableUiState.update {
                        it.copy(isLoading = false, message = e.message ?: "Error syncing")
                    }
                    return@launch
                }
                mutableUiState.update { it.copy(isLoading = false, message = "Sync completed") }
            }
        }
    }

    private fun updateProgress(value: Float) {
        mutableUiState.update { it.copy(syncProgress = value) }
    }

    init {
        App.dataStore.data
            .map { it[EXTERNAL_URI_KEY]?.let { uri -> Uri.parse(uri) } }
            .onEach { value ->
                mutableUiState.update { it.copy(externalSyncUri = value) }
            }
            .launchIn(viewModelScope)
    }
}