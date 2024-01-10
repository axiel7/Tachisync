package com.axiel7.tachisync.ui.external.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axiel7.tachisync.R

@Composable
fun ExternalDeviceItemView(
    deviceName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.usb_24),
            contentDescription = stringResource(R.string.device)
        )
        Text(
            text = deviceName,
            modifier = Modifier.padding(start = 8.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}