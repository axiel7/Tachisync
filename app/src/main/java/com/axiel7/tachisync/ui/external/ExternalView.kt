package com.axiel7.tachisync.ui.external

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.main.MainViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.FileUtils.areUriPermissionsGranted
import com.axiel7.tachisync.utils.SharedPrefsHelpers

const val EXTERNAL_STORAGE_DESTINATION = "external_storage"

@Composable
fun ExternalView(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val viewModel: ExternalViewModel = viewModel()
    val uriLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { uri ->
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            SharedPrefsHelpers.instance?.saveString("external_uri", uri.toString())
            mainViewModel.externalSyncUri = uri
        }
    }

    if (mainViewModel.externalSyncUri != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contents will be synced on: ${mainViewModel.externalSyncUri?.lastPathSegment}",
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    viewModel.reset(context)
                    mainViewModel.externalSyncUri = null
                    viewModel.getExternalStorages(context)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Select another directory")
            }
        }
    } else if (viewModel.externalStorages.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No external devices found",
                modifier = Modifier.padding(16.dp)
            )

            TextButton(onClick = { viewModel.getExternalStorages(context) }) {
                Icon(painter = painterResource(R.drawable.refresh_24), contentDescription = "refresh")
                Text(text = "Refresh", modifier = Modifier.padding(start = 4.dp))
            }
        }//:Column
    } else {
        Column {
            Text(
                text = "Select the device you want to sync on",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn {
                items(viewModel.externalStorages) {
                    ExternalDeviceItemView(
                        deviceName = it.getDescription(context),
                        onClick = {
                            viewModel.selectedDevice = it
                            viewModel.openExternalDirectoryHelpDialog = true
                        }
                    )
                }
            }//:LazyColumn
        }//:Column
    }

    if (viewModel.openExternalDirectoryHelpDialog) {
        ExternalDirectoryHelpDialog(viewModel = viewModel)
    }

    if (viewModel.openIntentForDirectory) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriLauncher.launch(viewModel.selectedDevice?.createOpenDocumentTreeIntent())
        } else {
            uriLauncher.launch(viewModel.selectedDevice?.createAccessIntent(null))
        }
        viewModel.openIntentForDirectory = false
    }

    LaunchedEffect(context) {
        val externalUri = SharedPrefsHelpers.instance?.getString("external_uri", null)
        if (externalUri.isNullOrEmpty() || !context.areUriPermissionsGranted(externalUri)) {
            viewModel.getExternalStorages(context)
        } else {
            mainViewModel.externalSyncUri = Uri.parse(externalUri)
        }
    }
}

@Composable
fun ExternalDeviceItemView(
    deviceName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.usb_24), contentDescription = "device")
        Text(
            text = deviceName,
            modifier = Modifier.padding(start = 8.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun ExternalDirectoryHelpDialog(viewModel: ExternalViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.openExternalDirectoryHelpDialog = false },
        title = { Text(text = "External directory") },
        text = { Text(text = "On the next screen, please navigate to the folder you want to sync your downloaded content and select 'Use this folder'") },
        confirmButton = {
            TextButton(onClick = {
                viewModel.openExternalDirectoryHelpDialog = false
                viewModel.openIntentForDirectory = true
            }) {
                Text(text = "OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ExternalPreview() {
    TachisyncTheme {
        ExternalView(mainViewModel = viewModel())
    }
}