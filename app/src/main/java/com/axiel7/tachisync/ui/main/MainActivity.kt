package com.axiel7.tachisync.ui.main

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.external.EXTERNAL_STORAGE_DESTINATION
import com.axiel7.tachisync.ui.external.ExternalView
import com.axiel7.tachisync.ui.files.FILES_DESTINATION
import com.axiel7.tachisync.ui.files.FilesView
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
    val viewModel: MainViewModel = viewModel()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { viewModel.syncContents(context) }) {
                Text(
                    text = viewModel.selectedCount.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Sync now", modifier = Modifier.padding(start = 8.dp))
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = FILES_DESTINATION,
            modifier = Modifier.padding(it)
        ) {
            composable(FILES_DESTINATION) { FilesView(mainViewModel = viewModel) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncingDialog(viewModel: MainViewModel) {
    AlertDialog(
        onDismissRequest = { },
    ) {
        Column {
            Text(text = "Syncing...", modifier = Modifier.padding(16.dp))
            LinearProgressIndicator(
                progress = viewModel.progress,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
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