package com.axiel7.tachisync.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.BaseViewModel
import java.io.File

class MainViewModel: BaseViewModel() {

    private var selectedManga = mutableListOf<Manga>()
    var selectedCount by mutableStateOf(0)

    fun onSelectedManga(manga: Manga, selected: Boolean) {
        if (selected) {
            selectedManga.add(manga)
            selectedCount++
        } else {
            selectedManga.remove(manga)
            selectedCount--
        }
    }

    var externalSyncDir: File? = null
}