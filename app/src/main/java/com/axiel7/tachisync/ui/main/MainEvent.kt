package com.axiel7.tachisync.ui.main

import android.content.Context
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.base.UiEvent

interface MainEvent : UiEvent {
    fun onExternalUriChanged(value: String)
    fun onTachiyomiUriChanged(value: String)
    fun syncContents(context: Context, contents: List<Manga>, selected: List<Int>)
}