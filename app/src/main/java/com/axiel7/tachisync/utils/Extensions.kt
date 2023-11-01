package com.axiel7.tachisync.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object Extensions {

    fun Context.openAction(uri: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            startActivity(this)
        }
    }
}