package com.axiel7.tachisync.ui.base

interface UiEvent {
    fun showMessage(message: String?)
    fun onMessageDisplayed()
}