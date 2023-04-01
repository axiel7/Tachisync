package com.axiel7.tachisync.ui.external

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.tachisync.ui.main.MainViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

const val EXTERNAL_STORAGE_DESTINATION = "external_storage"

@Composable
fun ExternalView(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val viewModel: ExternalViewModel = viewModel()
    var externalStorages by remember { mutableStateOf(listOf<StorageVolume>()) }
    var selectedDevice by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            text = "Selected device: ${selectedDevice ?: "None"}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedDevice == null) {
                items(externalStorages) {
                    Text(
                        text = it.getDescription(context),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    context.startActivity(it.createOpenDocumentTreeIntent())
                                } else {
                                    context.startActivity(it.createAccessIntent(null))
                                }
                            },
                    )
                }
            }
        }
    }

    LaunchedEffect(context) {
        this.launch(Dispatchers.IO) {
            externalStorages = getExternalStorages(context)
            //externalStorages[0].createOpenDocumentTreeIntent()
            //viewModel.directoryFiles = getExternalStorages(context)
        }
    }
}

fun getExternalStorages(context: Context): List<StorageVolume> {
    //return context.getExternalFilesDirs(null).asList()
    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    return storageManager.storageVolumes.filter { it.isRemovable && it.state == Environment.MEDIA_MOUNTED }
}

@Preview(showBackground = true)
@Composable
fun ExternalPreview() {
    TachisyncTheme {
        ExternalView(mainViewModel = viewModel())
    }
}