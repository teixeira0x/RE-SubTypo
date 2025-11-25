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

package com.teixeira0x.subtypo.ui.textlist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListIntent
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListUiEvent
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CueListViewModel
@Inject
constructor() : ViewModel() {

    companion object {
        private val cueTimeComparator =
            Comparator<Cue> { c1, c2 -> c1.startTime.compareTo(c2.startTime) }
    }

    private val _cueListUiState =
        MutableStateFlow<CueListUiState>(CueListUiState.Loaded(emptyList()))
    val cueListUiState: StateFlow<CueListUiState>
        get() = _cueListUiState.asStateFlow()

    private val _playerPosition = MutableLiveData(0L)
    val playerPosition: LiveData<Long> = _playerPosition
    private val _customUiEvent = MutableSharedFlow<CueListUiEvent>()
    val customUiEvent: SharedFlow<CueListUiEvent> = _customUiEvent.asSharedFlow()

    private val subtitleLive = MutableLiveData(Subtitle("subtitle", SubtitleFormat.of(".srt")))
    val subtitle: Subtitle
        get() = subtitleLive.value!!

    fun doIntent(intent: CueListIntent) {
        when (intent) {
            is CueListIntent.LoadSubtitle -> loadSubtitle(intent)
            is CueListIntent.ScrollTo -> scrollTo(intent)
            is CueListIntent.SortCueListByTime -> sortCueListByTime()
            is CueListIntent.PlayerPause -> playerPause()
            is CueListIntent.PlayerSeekTo -> playerSeekTo(intent)
        }
    }

    private fun loadSubtitle(intent: CueListIntent.LoadSubtitle) {
        _cueListUiState.value = CueListUiState.Loading
        viewModelScope.launch {
            subtitleLive.value = intent.subtitle
            _cueListUiState.value = CueListUiState.Loaded(intent.subtitle.data.cues)

            _customUiEvent.emit(CueListUiEvent.PlayerUpdateSubtitle(intent.subtitle))
        }
    }

    private fun scrollTo(intent: CueListIntent.ScrollTo) {
        viewModelScope.launch { _customUiEvent.emit(CueListUiEvent.ScrollTo(intent.index)) }
    }

    private fun sortCueListByTime() {
        viewModelScope.launch {
            val subtitle = this@CueListViewModel.subtitle

            val data =
                subtitle.data.copy(cues = subtitle.data.cues.sortedWith(cueTimeComparator))

            _cueListUiState.value = CueListUiState.Loaded(data.cues)

        }
    }

    private fun playerPause() {
        viewModelScope.launch { _customUiEvent.emit(CueListUiEvent.PlayerPause) }
    }

    private fun playerSeekTo(intent: CueListIntent.PlayerSeekTo) {
        viewModelScope.launch { _customUiEvent.emit(CueListUiEvent.PlayerSeekTo(intent.position)) }
    }

    fun updatePlayerPosition(position: Long) {
        _playerPosition.value = position
    }
}
