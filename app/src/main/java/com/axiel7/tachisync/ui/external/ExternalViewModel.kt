package com.axiel7.tachisync.ui.external

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.axiel7.tachisync.ui.base.BaseViewModel
import com.axiel7.tachisync.utils.FileUtils.releaseUriPermissions
import com.axiel7.tachisync.utils.SharedPrefsHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ExternalViewModel: BaseViewModel() {

    var externalStorages by mutableStateOf(listOf<StorageVolume>())

    fun getExternalStorages(context: Context) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            externalStorages = storageManager.storageVolumes.filter { it.isRemovable && it.state == Environment.MEDIA_MOUNTED }
            isLoading = false
        }
    }

    var selectedDevice: StorageVolume? = null
    var openIntentForDirectory by mutableStateOf(false)

    var openExternalDirectoryHelpDialog by mutableStateOf(false)

    fun reset(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            SharedPrefsHelpers.instance?.getString("external_uri", null)?.let {
                context.releaseUriPermissions(Uri.parse(it))
                SharedPrefsHelpers.instance?.deleteValue("external_uri")
            }
            externalStorages = emptyList()
            selectedDevice = null
        }
    }
}