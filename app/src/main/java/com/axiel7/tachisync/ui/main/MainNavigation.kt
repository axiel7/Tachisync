package com.axiel7.tachisync.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.axiel7.tachisync.ui.external.EXTERNAL_STORAGE_DESTINATION
import com.axiel7.tachisync.ui.external.ExternalView
import com.axiel7.tachisync.ui.files.FILES_DESTINATION
import com.axiel7.tachisync.ui.files.FilesEvent
import com.axiel7.tachisync.ui.files.FilesUiState
import com.axiel7.tachisync.ui.files.FilesView
import com.axiel7.tachisync.ui.settings.SETTINGS_DESTINATION
import com.axiel7.tachisync.ui.settings.SettingsView

@Composable
fun MainNavigation(
    navController: NavHostController,
    padding: PaddingValues,
    mainUiState: MainUiState,
    mainEvent: MainEvent?,
    filesUiState: FilesUiState,
    filesEvent: FilesEvent?,
) {
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
                filesUiState = filesUiState,
                filesEvent = filesEvent,
                mainEvent = mainEvent,
                contentPadding = PaddingValues(
                    top = topPadding,
                    bottom = bottomPadding
                )
            )
        }

        composable(EXTERNAL_STORAGE_DESTINATION) {
            ExternalView(
                mainUiState = mainUiState,
                mainEvent = mainEvent,
                modifier = Modifier.padding(
                    top = topPadding,
                    bottom = bottomPadding
                )
            )
        }

        composable(SETTINGS_DESTINATION) {
            SettingsView(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}