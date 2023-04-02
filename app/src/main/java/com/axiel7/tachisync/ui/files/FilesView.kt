package com.axiel7.tachisync.ui.files

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.tachisync.R
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.main.MainViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.Extensions.getActivity
import com.axiel7.tachisync.utils.FileUtils.areUriPermissionsGranted
import com.axiel7.tachisync.utils.SharedPrefsHelpers
import java.io.File

const val FILES_DESTINATION = "files"

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilesView(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val viewModel: FilesViewModel = viewModel()
    val pullRefreshState = rememberPullRefreshState(viewModel.isLoading, { viewModel.refresh(context) })
    val uriLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { uri ->
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            SharedPrefsHelpers.instance?.saveString("tachiyomi_uri", uri.toString())
            mainViewModel.tachiyomiUri = uri
            viewModel.readDownloadsDir(uri, context)
        }
    }

    Box(
        modifier = Modifier
            .clipToBounds()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.downloadedManga) { manga ->
                SelectableMangaItemView(manga = manga, onClick = { selected ->
                    mainViewModel.onSelectedManga(manga, selected)
                })
            }
        }

        PullRefreshIndicator(viewModel.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }

    if (viewModel.openIntentForDirectory) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            uriLauncher.launch(this)
        }
        viewModel.openIntentForDirectory = false
    }

    if (viewModel.openTachiyomiDirectoryHelpDialog) {
        TachiyomiDirectoryHelpDialog(viewModel = viewModel)
    }

    if (viewModel.showMessage) {
        Toast.makeText(context, viewModel.message, Toast.LENGTH_SHORT).show()
        viewModel.showMessage = false
    }

    LaunchedEffect(context) {
        viewModel.refresh(context)
    }
}

@Composable
fun SelectableMangaItemView(
    manga: Manga,
    onClick: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clickable {
                isChecked = !isChecked
                onClick(isChecked)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onClick(it)
                }
            )
            Text(
                text = manga.name,
                modifier = Modifier.padding(8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Text(text = manga.chapters.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun TachiyomiDirectoryHelpDialog(viewModel: FilesViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.openTachiyomiDirectoryHelpDialog = false },
        title = { Text(text = "Downloads directory") },
        text = { Text(text = "On the next screen, please navigate to the folder containing your Tachiyomi downloads (usually /Tachiyomi/downloads/) and select 'Use this folder'") },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.openTachiyomiDirectoryHelpDialog = false
                    viewModel.openIntentForDirectory = true
                }
            ) {
                Text(text = "OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FilesPreview() {
    TachisyncTheme {
        FilesView(mainViewModel = viewModel())
    }
}