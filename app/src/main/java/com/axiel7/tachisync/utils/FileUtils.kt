package com.axiel7.tachisync.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.MutableState
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
        contentResolver.releasePersistableUriPermission(uri, flags)
    }

    fun Context.syncDirectory(
        sourceDir: DocumentFile,
        destRootDir: DocumentFile,
        progress: MutableState<Float>,
        currentFileCount: MutableState<Int>,
        total: Float,
    ) {
        if (sourceDir.isDirectory && destRootDir.isDirectory && sourceDir.name != null) {
            if (sourceDir.name!!.endsWith("_tmp")) return
            // Check if the directory already exist
            var destDir = destRootDir.findFile(sourceDir.name!!)
            if (destDir == null) {
                // Create the destination directory if it doesn't exist
                destDir = destRootDir.createDirectory(sourceDir.name!!)
            }

            // Copy each file or directory to the destination directory
            sourceDir.listFiles().forEach { file ->
                if (file.name != null) {
                    if (file.isDirectory) {
                        // If the file is a directory, recursively call this function
                        val childSourceDir = sourceDir.findFile(file.name!!)
                        val childDestDir = destDir?.findFile(file.name!!)
                        if (childSourceDir != null && childDestDir != null)
                            syncDirectory(childSourceDir, childDestDir, progress, currentFileCount, total)

                    } else {
                        currentFileCount.value += 1
                        // If the file is a regular file, copy its contents to the destination file
                        // Check if the file already exist
                        var destFile = destDir?.findFile(file.name!!)
                        if (destFile == null && file.type != null) {
                            destFile = destDir?.createFile(file.type!!, file.name!!)
                        }
                        if (destFile ?.uri != null) {
                            val inputStream = contentResolver.openInputStream(file.uri)
                            val outputStream = contentResolver.openOutputStream(destFile.uri)
                            if (outputStream != null) inputStream?.copyTo(outputStream)
                            inputStream?.close()
                            outputStream?.close()
                        }
                        progress.value = currentFileCount.value.div(total)
                    }
                }
            }
        }
    }

}