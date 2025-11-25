/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.textedit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditIntent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditUiEvent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditUiState
import com.teixeira0x.subtypo.ui.textedit.util.CueFieldType
import com.teixeira0x.subtypo.ui.textedit.validate.CueValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class CueEditViewModel
@Inject
constructor(
    private val cueValidator: CueValidator,
) : ViewModel() {

    private val _cueEditUiState = MutableLiveData<CueEditUiState>(CueEditUiState.Loading)
    val cueEditUiState: LiveData<CueEditUiState>
        get() = _cueEditUiState

    private val _customUiEvent = MutableSharedFlow<CueEditUiEvent>()
    val customUiEvent: SharedFlow<CueEditUiEvent> = _customUiEvent.asSharedFlow()

    private var subtitle: Subtitle? = null

    val subtitleTimeFormat: String
        get() = subtitle?.format?.timeFormat ?: "hh:mm:ss,SSS"

    fun doIntent(event: CueEditIntent) {
        when (event) {
            is CueEditIntent.LoadCue -> loadCue(event)
            is CueEditIntent.ValidateCueField -> validateCueField(event)
        }
    }

    private fun loadCue(event: CueEditIntent.LoadCue) {
        _cueEditUiState.value = CueEditUiState.Loading
        viewModelScope.launch {
            subtitle = event.subtitle
            _cueEditUiState.value = CueEditUiState.Loaded(event.subtitle.data.cues.getOrNull(event.cueIndex))
        }
    }

    private fun validateCueField(event: CueEditIntent.ValidateCueField) {
        viewModelScope.launch {
            val validationResult =
                when (event.type) {
                    CueFieldType.START_TIME,
                    CueFieldType.END_TIME -> cueValidator.checkTime(event.text, subtitleTimeFormat)
                    CueFieldType.TEXT -> cueValidator.checkText(event.text)
                }

            _customUiEvent.emit(CueEditUiEvent.UpdateField(event.type, validationResult))
        }
    }
}
