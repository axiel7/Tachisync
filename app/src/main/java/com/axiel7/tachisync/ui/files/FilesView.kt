package com.axiel7.tachisync.ui.files

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axiel7.tachisync.R
import com.axiel7.tachisync.data.model.Manga
import com.axiel7.tachisync.ui.main.MainViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.FileUtils.rememberUriLauncher

const val FILES_DESTINATION = "files"

@Composable
fun FilesView(
    filesViewModel: FilesViewModel,
    mainViewModel: MainViewModel,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val uriLauncher = rememberUriLauncher { uri ->
        mainViewModel.onTachiyomiUriChanged(uri.toString())
        filesViewModel.readDownloadsDir(uri, context)
    }

    if (filesViewModel.openIntentForDirectory) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            uriLauncher.launch(this)
        }
        filesViewModel.openIntentForDirectory = false
    }

    if (filesViewModel.openTachiyomiDirectoryHelpDialog) {
        TachiyomiDirectoryHelpDialog(viewModel = filesViewModel)
    }

    if (filesViewModel.showMessage) {
        Toast.makeText(context, filesViewModel.message, Toast.LENGTH_SHORT).show()
        filesViewModel.showMessage = false
    }

    LaunchedEffect(context) {
        if (filesViewModel.downloadedManga.isEmpty()) filesViewModel.refresh(context)
    }

    Box(
        modifier = Modifier.clipToBounds()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
        ) {
            itemsIndexed(
                items = filesViewModel.downloadedManga,
                key = { _, manga -> manga.file.uri }
            ) { index, manga ->
                SelectableMangaItemView(
                    manga = manga,
                    onClick = { selected ->
                        filesViewModel.onSelectedManga(index, selected)
                    }
                )
            }
        }

        if (filesViewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun SelectableMangaItemView(
    manga: Manga,
    onClick: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(manga.isSelected) }
    Row(
        modifier = Modifier
            .clickable {
                isChecked = !manga.isSelected
                onClick(isChecked)
            }
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = manga.isSelected,
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
        title = { Text(text = stringResource(R.string.download_directory)) },
        text = { Text(text = stringResource(R.string.downloads_directory_explanation)) },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.openTachiyomiDirectoryHelpDialog = false
                    viewModel.openIntentForDirectory = true
                }
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FilesPreview() {
    TachisyncTheme {
        FilesView(filesViewModel = viewModel(), mainViewModel = viewModel())
    }
}