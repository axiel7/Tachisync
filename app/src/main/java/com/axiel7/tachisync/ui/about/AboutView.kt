package com.axiel7.tachisync.ui.about

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.axiel7.tachisync.ui.theme.TachisyncTheme
import com.axiel7.tachisync.utils.Extensions.openAction

const val ABOUT_DESTINATION = "about"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutView(
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.about))},
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(painter = painterResource(R.drawable.arrow_back_24), contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
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
            Spacer(modifier = Modifier
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

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    TachisyncTheme {
        AboutView(navigateBack = {})
    }
}