package com.axiel7.tachisync

import android.app.Application
import com.axiel7.tachisync.utils.SharedPrefsHelpers

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefsHelpers.init(applicationContext)
    }
}