package com.axiel7.tachisync.ui.main

import android.net.Uri
import com.axiel7.tachisync.ui.base.UiState

data class MainUiState(
    val syncProgress: Float = 0f,
    val externalSyncUri: Uri? = null,
    override val isLoading: Boolean = false,
    override val message: String? = null
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
