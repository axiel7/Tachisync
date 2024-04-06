package com.axiel7.tachisync.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axiel7.tachisync.BuildConfig
import com.axiel7.tachisync.R
import com.axiel7.tachisync.data.datastore.PreferencesDataStore.REMOVE_SCANLATOR_KEY
import com.axiel7.tachisync.data.datastore.PreferencesRepository
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.Extensions.openAction
import kotlinx.coroutines.launch

const val SETTINGS_DESTINATION = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    resetDownloadsDirectory: () -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val removeScanlator by PreferencesRepository.remember(REMOVE_SCANLATOR_KEY)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SwitchPreference(
                title = "Remove scanlator from filename",
                value = removeScanlator,
                onValueChange = {
                    scope.launch {
                        PreferencesRepository.set(REMOVE_SCANLATOR_KEY, it)
                    }
                }
            )
            AboutItem(
                title = stringResource(R.string.reset_downloads_directory),
                icon = R.drawable.sync_24,
                onClick = resetDownloadsDirectory
            )
            AboutItem(
                title = "GitHub",
                subtitle = "Source code",
                icon = R.drawable.code_24,
                onClick = { context.openAction("https://github.com/axiel7/Tachisync") }
            )

            AboutItem(
                title = stringResource(R.string.developer),
                subtitle = "axiel7",
                icon = R.drawable.person_24,
                onClick = { context.openAction("https://github.com/axiel7") }
            )

            AboutItem(
                title = stringResource(R.string.version),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
        }
    }
}

@Composable
fun AboutItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes icon: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "",
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
            )
        }

        Column(
            modifier = if (subtitle != null)
                Modifier.padding(16.dp)
            else Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: Boolean?,
    @DrawableRes icon: Int? = null,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onValueChange(value?.not() ?: false)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = if (subtitle != null)
                    Modifier.padding(16.dp)
                else Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 14.sp
                    )
                }
            }//: Column
        }//: Row

        Switch(
            checked = value ?: false,
            onCheckedChange = onValueChange,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }//: Row
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    TachisyncTheme {
        SettingsView(
            resetDownloadsDirectory = {},
            navigateBack = {}
        )
    }
}