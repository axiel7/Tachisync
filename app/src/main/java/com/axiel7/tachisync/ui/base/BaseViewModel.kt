package com.axiel7.tachisync.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S : UiState> : ViewModel(), UiEvent {

    protected abstract val mutableUiState: MutableStateFlow<S>
    val uiState: StateFlow<S> get() = mutableUiState.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun setLoading(value: Boolean) {
        mutableUiState.update { it.setLoading(value) as S }
    }

    @Suppress("UNCHECKED_CAST")
    override fun showMessage(message: String?) {
        mutableUiState.update { it.setMessage(message) as S }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onMessageDisplayed() {
        mutableUiState.update { it.setMessage(null) as S }
    }
}