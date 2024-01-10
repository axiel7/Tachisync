package com.axiel7.tachisync.ui.main.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.axiel7.tachisync.R

@Composable
fun SyncingDialog(progress: Float) {
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
                progress = { progress },
                modifier = Modifier.padding(8.dp)
            )
        }
    )
}