package com.axiel7.tachisync.ui.files

import android.content.Context
import android.net.Uri
import com.axiel7.tachisync.ui.base.UiEvent

interface FilesEvent : UiEvent {
    fun onSelectedManga(index: Int, selected: Boolean)
    fun selectAllManga()
    fun deselectAllManga()
    fun refresh(context: Context)
    fun readDownloadsDir(downloadsUri: Uri, context: Context)
    fun setOpenIntentForDirectory(value: Boolean)
    fun setOpenTachiyomiDirectoryHelpDialog(value: Boolean)
}