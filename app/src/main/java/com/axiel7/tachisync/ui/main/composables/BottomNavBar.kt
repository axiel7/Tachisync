package com.axiel7.tachisync.ui.main.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.axiel7.tachisync.R
import com.axiel7.tachisync.ui.external.EXTERNAL_STORAGE_DESTINATION
import com.axiel7.tachisync.ui.files.FILES_DESTINATION

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