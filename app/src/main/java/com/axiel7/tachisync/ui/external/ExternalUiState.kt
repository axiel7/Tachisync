package com.axiel7.tachisync.ui.external

import android.os.storage.StorageVolume
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.axiel7.tachisync.ui.base.UiState

@Stable
data class ExternalUiState(
    val externalStorages: SnapshotStateList<StorageVolume> = mutableStateListOf(),
    val selectedDevice: StorageVolume? = null,
    val openIntentForDirectory: Boolean = false,
    val openExternalDirectoryHelpDialog: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
