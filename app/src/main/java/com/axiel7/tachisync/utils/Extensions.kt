package com.axiel7.tachisync.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

object Extensions {

    fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

}