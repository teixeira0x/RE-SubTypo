package com.teixeira0x.subtypo.ui.sourceview.viewmodel

import androidx.lifecycle.ViewModel
import com.teixeira0x.subtypo.ui.sourceview.mvp.SourceViewIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SourceViewViewModel : ViewModel() {

    private val _sourceTextUiState = MutableStateFlow(SourceTextUiState())

    val sourceTextUiState: StateFlow<SourceTextUiState> = _sourceTextUiState.asStateFlow()

    fun doIntent(intent: SourceViewIntent) {
        when (intent) {
            is SourceViewIntent.LoadSubtitle -> loadSubtitle(intent)
            is SourceViewIntent.UpdateText -> updateText(intent)
        }
    }

    private fun loadSubtitle(intent: SourceViewIntent.LoadSubtitle) {
        _sourceTextUiState.value = _sourceTextUiState.value.copy(text = intent.subtitle.toText())
    }

    private fun updateText(intent: SourceViewIntent.UpdateText) {
        _sourceTextUiState.value = _sourceTextUiState.value.copy(
            text = intent.text,
            isFromUi = true
        )
    }

    data class SourceTextUiState(
        val text: String = "",
        val isFromUi: Boolean = false
    )
}