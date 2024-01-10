package com.axiel7.tachisync.ui.external

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.App
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.EXTERNAL_URI_KEY
import com.axiel7.tachisync.data.datastore.PreferencesRepository
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.releaseUriPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExternalViewModel : BaseViewModel<ExternalUiState>(), ExternalEvent {

    override val mutableUiState = MutableStateFlow(ExternalUiState())

    override fun getExternalStorages(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            mutableUiState.value.run {
                externalStorages.clear()
                externalStorages.addAll(
                    storageManager.storageVolumes
                        .filter { it.isRemovable && it.state == Environment.MEDIA_MOUNTED }
                )
            }
            setLoading(false)
        }
    }

    override fun onDeviceSelected(device: StorageVolume?) {
        mutableUiState.update { it.copy(selectedDevice = device) }
    }

    override fun setOpenIntentForDirectory(value: Boolean) {
        mutableUiState.update { it.copy(openIntentForDirectory = value) }
    }

    override fun setOpenExternalDirectoryHelpDialog(value: Boolean) {
        mutableUiState.update { it.copy(openExternalDirectoryHelpDialog = value) }
    }

    override fun reset() {
        viewModelScope.launch(Dispatchers.IO) {
            PreferencesRepository.get(EXTERNAL_URI_KEY)?.let { uri ->
                App.applicationContext.releaseUriPermissions(Uri.parse(uri))
                PreferencesRepository.remove(EXTERNAL_URI_KEY)
            }
            mutableUiState.update {
                it.externalStorages.clear()
                it.copy(selectedDevice = null)
            }
        }
    }
}