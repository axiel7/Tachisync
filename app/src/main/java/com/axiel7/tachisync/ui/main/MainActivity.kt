package com.axiel7.tachisync.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.about.ABOUT_DESTINATION
import com.axiel7.tachisync.ui.files.FILES_DESTINATION
import com.axiel7.tachisync.ui.files.FilesEvent
import com.axiel7.tachisync.ui.files.FilesUiState
import com.axiel7.tachisync.ui.files.FilesViewModel
import com.axiel7.tachisync.ui.main.composables.BottomNavBar
import com.axiel7.tachisync.ui.main.composables.SyncingDialog
import com.axiel7.tachisync.ui.theme.TachisyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val mainUiState by mainViewModel.uiState.collectAsState()
            val filesViewModel: FilesViewModel = viewModel()
            val filesUiState by filesViewModel.uiState.collectAsState()

            TachisyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainView(
                        mainEvent = mainViewModel,
                        mainUiState = mainUiState,
                        filesEvent = filesViewModel,
                        filesUiState = filesUiState,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    mainEvent: MainEvent?,
    mainUiState: MainUiState,
    filesEvent: FilesEvent?,
    filesUiState: FilesUiState,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val snackbarState = remember { SnackbarHostState() }
    val isFullScreenDestination by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route == ABOUT_DESTINATION
        }
    }
    val showEditToolbar by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route == FILES_DESTINATION
                    && filesUiState.selectedMangaIndices.isNotEmpty()
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isFullScreenDestination,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        if (showEditToolbar) {
                            Text(
                                text = stringResource(
                                    R.string.num_selected,
                                    filesUiState.selectedMangaIndices.size.toString()
                                )
                            )
                        } else {
                            Text(text = stringResource(R.string.app_name))
                        }
                    },
                    navigationIcon = {
                        if (showEditToolbar) {
                            IconButton(onClick = { filesEvent?.deselectAllManga() }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_24),
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        }
                    },
                    actions = {
                        if (showEditToolbar) {
                            IconButton(onClick = { filesEvent?.selectAllManga() }) {
                                Icon(
                                    painter = painterResource(R.drawable.select_all_24),
                                    contentDescription = stringResource(R.string.select_all)
                                )
                            }
                        } else {
                            if (navBackStackEntry?.destination?.route == FILES_DESTINATION) {
                                IconButton(onClick = { filesEvent?.refresh(context) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.refresh_24),
                                        contentDescription = stringResource(R.string.refresh)
                                    )
                                }
                            }
                            IconButton(onClick = { navController.navigate(ABOUT_DESTINATION) }) {
                                Icon(
                                    painter = painterResource(R.drawable.help_outline_24),
                                    contentDescription = stringResource(R.string.about)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (showEditToolbar)
                            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        else MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isFullScreenDestination,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it / 2 }
            ) {
                BottomNavBar(navController = navController)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isFullScreenDestination,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        mainEvent?.syncContents(
                            context,
                            filesUiState.downloadedManga,
                            filesUiState.selectedMangaIndices
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sync_24),
                        contentDescription = stringResource(R.string.sync)
                    )
                    Text(
                        text = stringResource(R.string.sync),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        MainNavigation(
            navController = navController,
            padding = padding,
            mainUiState = mainUiState,
            mainEvent = mainEvent,
            filesUiState = filesUiState,
            filesEvent = filesEvent,
        )
    }

    LaunchedEffect(mainUiState.message, filesUiState.message) {
        if (mainUiState.message != null) {
            snackbarState.showSnackbar(message = mainUiState.message)
            mainEvent?.onMessageDisplayed()
        } else if (filesUiState.message != null) {
            snackbarState.showSnackbar(message = filesUiState.message)
            filesEvent?.onMessageDisplayed()
        }
    }

    if (mainUiState.isLoading) {
        SyncingDialog(
            progress = mainUiState.syncProgress
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TachisyncTheme {
        Surface {
            MainView(
                mainEvent = null,
                mainUiState = MainUiState(),
                filesEvent = null,
                filesUiState = FilesUiState(),
            )
        }
    }
}