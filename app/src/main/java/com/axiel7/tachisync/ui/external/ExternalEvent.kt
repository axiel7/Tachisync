package com.axiel7.tachisync.ui.external

import android.content.Context
import android.os.storage.StorageVolume
import com.axiel7.tachisync.ui.base.UiEvent

interface ExternalEvent : UiEvent {
    fun getExternalStorages(context: Context)
    fun onDeviceSelected(device: StorageVolume?)
    fun setOpenIntentForDirectory(value: Boolean)
    fun setOpenExternalDirectoryHelpDialog(value: Boolean)
    fun reset()
}