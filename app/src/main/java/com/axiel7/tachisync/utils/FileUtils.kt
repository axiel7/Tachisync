package com.axiel7.tachisync.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

object FileUtils {

    fun Context.areUriPermissionsGranted(uriString: String): Boolean {
        val list = contentResolver.persistedUriPermissions
        for (index in list.indices) {
            val persistedUriString = list[index].uri.toString()
            if (persistedUriString == uriString && list[index].isWritePermission && list[index].isReadPermission) {
                return true
            }
        }
        return false
    }

    fun Context.releaseUriPermissions(uri: Uri) {
        val flags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.releasePersistableUriPermission(uri,flags)

        SharedPrefsHelpers.instance?.deleteValue("tachiyomi_uri")
    }

}