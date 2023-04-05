package com.axiel7.tachisync.ui.main

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.external.EXTERNAL_STORAGE_DESTINATION
import com.axiel7.tachisync.ui.external.ExternalView
import com.axiel7.tachisync.ui.files.FILES_DESTINATION
import com.axiel7.tachisync.ui.files.FilesView
import com.axiel7.tachisync.ui.files.FilesViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.SharedPrefsHelpers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TachisyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val viewModel: MainViewModel = viewModel()
    val filesViewModel: FilesViewModel = viewModel()
    val showEditToolbar by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route == FILES_DESTINATION
                    && filesViewModel.selectedCount > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showEditToolbar) {
                        Text(text = "${filesViewModel.selectedCount} selected")
                    } else {
                        Text(text = stringResource(R.string.app_name))
                    }
                },
                navigationIcon = {
                    if (showEditToolbar) {
                        IconButton(onClick = { filesViewModel.deselectAllManga() }) {
                            Icon(painter = painterResource(R.drawable.close_24), contentDescription = "close")
                        }
                    }
                },
                actions = {
                    if (showEditToolbar) {
                        IconButton(onClick = { filesViewModel.selectAllManga() }) {
                            Icon(painter = painterResource(R.drawable.select_all_24), contentDescription = "select all")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showEditToolbar) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                else MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.syncContents(context, filesViewModel.downloadedManga, filesViewModel.selectedManga)
                }
            ) {
                Icon(painter = painterResource(R.drawable.sync_24), contentDescription = "sync")
                Text(text = "Sync now", modifier = Modifier.padding(start = 8.dp))
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = FILES_DESTINATION,
            modifier = Modifier.padding(it)
        ) {
            composable(FILES_DESTINATION) {
                FilesView(filesViewModel = filesViewModel, mainViewModel = viewModel)
            }

            composable(EXTERNAL_STORAGE_DESTINATION) { ExternalView(mainViewModel = viewModel) }
        }
    }

    if (viewModel.showMessage) {
        Toast.makeText(context, viewModel.message, Toast.LENGTH_SHORT).show()
        viewModel.showMessage = false
    }

    if (viewModel.isSyncing) {
        SyncingDialog(viewModel = viewModel)
    }

    LaunchedEffect(context) {
        SharedPrefsHelpers.instance?.getString("external_uri", null)?.let {
            viewModel.externalSyncUri = Uri.parse(it)
        }
        SharedPrefsHelpers.instance?.getString("tachiyomi_uri", null)?.let {
            viewModel.tachiyomiUri = Uri.parse(it)
        }
    }

}

@Composable
fun SyncingDialog(viewModel: MainViewModel) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        title = { Text(text = "Syncing...", modifier = Modifier.padding(16.dp)) },
        text = {
            LinearProgressIndicator(
                progress = viewModel.progress,
                modifier = Modifier.padding(8.dp)
            )
        }
    )
}

@Composable
fun BottomNavBar(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = {
                selectedItem = 0
                navController.navigate(FILES_DESTINATION)
            },
            icon = { Icon(painter = painterResource(R.drawable.download_24), contentDescription = "download") },
            label = { Text(text = "Downloads") }
        )

        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = {
                selectedItem = 1
                navController.navigate(EXTERNAL_STORAGE_DESTINATION)
            },
            icon = { Icon(painter = painterResource(R.drawable.storage_24), contentDescription = "storage") },
            label = { Text(text = "External") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TachisyncTheme {
        MainView()
    }
}