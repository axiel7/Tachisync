package com.axiel7.tachisync.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.about.ABOUT_DESTINATION
import com.axiel7.tachisync.ui.about.AboutView
import com.axiel7.tachisync.ui.external.EXTERNAL_STORAGE_DESTINATION
import com.axiel7.tachisync.ui.external.ExternalView
import com.axiel7.tachisync.ui.files.FILES_DESTINATION
import com.axiel7.tachisync.ui.files.FilesView
import com.axiel7.tachisync.ui.files.FilesViewModel
import com.axiel7.tachisync.ui.theme.TachisyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TachisyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
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
    val isFullScreenDestination by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route == ABOUT_DESTINATION
        }
    }
    val showEditToolbar by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route == FILES_DESTINATION
                    && filesViewModel.selectedCount > 0
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
                                    filesViewModel.selectedCount.toString()
                                )
                            )
                        } else {
                            Text(text = stringResource(R.string.app_name))
                        }
                    },
                    navigationIcon = {
                        if (showEditToolbar) {
                            IconButton(onClick = { filesViewModel.deselectAllManga() }) {
                                Icon(
                                    painter = painterResource(R.drawable.close_24),
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        }
                    },
                    actions = {
                        if (showEditToolbar) {
                            IconButton(onClick = { filesViewModel.selectAllManga() }) {
                                Icon(
                                    painter = painterResource(R.drawable.select_all_24),
                                    contentDescription = stringResource(R.string.select_all)
                                )
                            }
                        } else {
                            if (navBackStackEntry?.destination?.route == FILES_DESTINATION) {
                                IconButton(onClick = { filesViewModel.refresh(context) }) {
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
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isFullScreenDestination,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.syncContents(
                            context,
                            filesViewModel.downloadedManga,
                            filesViewModel.selectedManga
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
        val topPadding by animateDpAsState(
            targetValue = padding.calculateTopPadding(),
            label = "top_bar_padding"
        )
        val bottomPadding by animateDpAsState(
            targetValue = padding.calculateBottomPadding(),
            label = "bottom_bar_padding"
        )
        NavHost(
            navController = navController,
            startDestination = FILES_DESTINATION,
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
            ),
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(400)) }
        ) {
            composable(FILES_DESTINATION) {
                FilesView(
                    filesViewModel = filesViewModel,
                    mainViewModel = viewModel,
                    contentPadding = PaddingValues(
                        top = topPadding,
                        bottom = bottomPadding
                    )
                )
            }

            composable(EXTERNAL_STORAGE_DESTINATION) {
                ExternalView(
                    mainViewModel = viewModel,
                    modifier = Modifier.padding(
                        top = topPadding,
                        bottom = bottomPadding
                    )
                )
            }

            composable(ABOUT_DESTINATION) {
                AboutView(
                    navigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    if (viewModel.showMessage) {
        Toast.makeText(context, viewModel.message, Toast.LENGTH_SHORT).show()
        viewModel.showMessage = false
    }

    if (viewModel.isSyncing) {
        SyncingDialog(viewModel = viewModel)
    }
}

@Composable
fun SyncingDialog(viewModel: MainViewModel) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        title = {
            Text(
                text = stringResource(R.string.syncing),
                modifier = Modifier.padding(16.dp)
            )
        },
        text = {
            LinearProgressIndicator(
                progress = viewModel.progress.floatValue,
                modifier = Modifier.padding(8.dp)
            )
        }
    )
}

@Composable
fun BottomNavBar(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = {
                selectedItem = 0
                navController.navigate(FILES_DESTINATION) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.download_24),
                    contentDescription = stringResource(R.string.downloads)
                )
            },
            label = { Text(text = stringResource(R.string.downloads)) }
        )

        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = {
                selectedItem = 1
                navController.navigate(EXTERNAL_STORAGE_DESTINATION) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.storage_24),
                    contentDescription = stringResource(R.string.external)
                )
            },
            label = { Text(text = stringResource(R.string.external)) }
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