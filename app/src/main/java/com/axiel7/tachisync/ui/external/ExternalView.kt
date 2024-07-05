package com.axiel7.tachisync.ui.external

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.composables.MessageDialog
import com.axiel7.tachisync.ui.external.composables.ExternalDeviceItemView
import com.axiel7.tachisync.ui.main.MainEvent
import com.axiel7.tachisync.ui.main.MainUiState
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.FileUtils.areUriPermissionsGranted
import com.axiel7.tachisync.utils.FileUtils.rememberUriLauncher

const val EXTERNAL_STORAGE_DESTINATION = "external_storage"

@Composable
fun ExternalView(
    mainUiState: MainUiState,
    mainEvent: MainEvent?,
    modifier: Modifier = Modifier,
) {
    val viewModel: ExternalViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    ExternalContent(
        mainUiState = mainUiState,
        mainEvent = mainEvent,
        externalUiState = uiState,
        externalEvent = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun ExternalContent(
    mainUiState: MainUiState,
    mainEvent: MainEvent?,
    externalUiState: ExternalUiState,
    externalEvent: ExternalEvent?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val uriLauncher = rememberUriLauncher { uri ->
        mainEvent?.onExternalUriChanged(uri.toString())
    }

    LaunchedEffect(mainUiState.externalSyncUri) {
        if (mainUiState.externalSyncUri == null
            || !context.areUriPermissionsGranted(mainUiState.externalSyncUri.toString())
        ) {
            externalEvent?.getExternalStorages(context)
        }
    }

    if (externalUiState.openExternalDirectoryHelpDialog) {
        MessageDialog(
            title = stringResource(R.string.external_directory),
            message = stringResource(R.string.external_directory_explanation),
            onConfirm = {
                externalEvent?.setOpenExternalDirectoryHelpDialog(false)
                externalEvent?.setOpenIntentForDirectory(true)
            },
            onDismiss = { externalEvent?.setOpenExternalDirectoryHelpDialog(false) }
        )
    }

    if (externalUiState.openIntentForDirectory && externalUiState.selectedDevice != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriLauncher.launch(externalUiState.selectedDevice.createOpenDocumentTreeIntent())
        } else {
            @Suppress("DEPRECATION")
            externalUiState.selectedDevice.createAccessIntent(null)
                ?.let { uriLauncher.launch(it) }
        }
        externalEvent?.setOpenIntentForDirectory(false)
    }

    if (mainUiState.externalSyncUri != null) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    R.string.contents_synced_on,
                    mainUiState.externalSyncUri.lastPathSegment.orEmpty()
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    externalEvent?.reset()
                    externalEvent?.getExternalStorages(context)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = stringResource(R.string.select_another_directory))
            }
        }
    } else if (externalUiState.externalStorages.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_external_found),
                modifier = Modifier.padding(16.dp)
            )

            TextButton(onClick = { externalEvent?.getExternalStorages(context) }) {
                Icon(
                    painter = painterResource(R.drawable.refresh_24),
                    contentDescription = stringResource(R.string.refresh)
                )
                Text(
                    text = stringResource(R.string.refresh),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }//:Column
    } else {
        Column(modifier = modifier) {
            Text(
                text = stringResource(R.string.select_device_to_sync),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn {
                items(externalUiState.externalStorages) {
                    ExternalDeviceItemView(
                        deviceName = it.getDescription(context),
                        onClick = {
                            externalEvent?.onDeviceSelected(it)
                            externalEvent?.setOpenExternalDirectoryHelpDialog(true)
                        }
                    )
                }
            }//:LazyColumn
        }//:Column
    }
}

@Preview(showBackground = true)
@Composable
fun ExternalPreview() {
    TachisyncTheme {
        Surface {
            ExternalContent(
                mainUiState = MainUiState(),
                mainEvent = null,
                externalUiState = ExternalUiState(),
                externalEvent = null,
            )
        }
    }
}