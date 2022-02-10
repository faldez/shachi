package com.faldez.shachi.ui.search_advanced

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchAdvancedViewModel : ViewModel() {
    val state: MutableStateFlow<UiState> = MutableStateFlow(UiState(tags = ""))
}

data class UiState(
    val tags: String,
)