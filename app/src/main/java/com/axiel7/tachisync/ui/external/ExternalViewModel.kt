package com.axiel7.tachisync.ui.external

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.axiel7.tachisync.ui.base.BaseViewModel
import java.io.File

class ExternalViewModel: BaseViewModel() {

    var directoryFiles by mutableStateOf(listOf<File>())
}