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
import com.teixeira0x.subtypo.ui.textlist.model.SimpleSubtitleData
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CueListViewModel @Inject constructor() : ViewModel() {

    companion object {
        private val cueTimeComparator =
            Comparator<Cue> { c1, c2 -> c1.startTime.compareTo(c2.startTime) }
    }

    private val _customUiEvent = MutableSharedFlow<CueListUiEvent>()
    val customUiEvent: SharedFlow<CueListUiEvent> = _customUiEvent.asSharedFlow()

    private val _subtitleData =
        MutableLiveData(SimpleSubtitleData("subtitle", SubtitleFormat.of(".srt")))

    val subtitleName: String
        get() = _subtitleData.value!!.name
    val subtitleFormat: SubtitleFormat
        get() = _subtitleData.value!!.format
    val subtitleExtras: Map<String, String>?
        get() = _subtitleData.value!!.extras

    private val _cues = MutableLiveData(listOf<Cue>())

    val cues: LiveData<List<Cue>>
        get() = _cues

    fun loadSubtitle(
        name: String, format: SubtitleFormat, cues: List<Cue>, extras: Map<String, String>? = null
    ) {
        viewModelScope.launch {
            _subtitleData.value = _subtitleData.value!!.copy(
                name = name, format = format, extras = extras
            )
            _cues.value = cues
        }
    }


    fun sortCueListByTime() {
        viewModelScope.launch {
            val cues = _cues.value!!.toMutableList()
            cues.sortWith(cueTimeComparator)
            _cues.value = cues
        }
    }

    fun scrollTo(index: Int) {
        viewModelScope.launch { _customUiEvent.emit(CueListUiEvent.ScrollTo(index)) }
    }

    fun setSubtitleName(name: String) = viewModelScope.launch {
        _subtitleData.value = _subtitleData.value!!.copy(
            name = name,
        )
    }

    fun setSubtitleFormat(format: SubtitleFormat) {
        _subtitleData.value = _subtitleData.value!!.copy(
            format = format,
        )
    }

    fun setCues(cues: List<Cue>) = viewModelScope.launch {

        _cues.value = cues
    }

    fun addCue(index: Int, cue: Cue) {
        viewModelScope.launch {
            val cues = _cues.value!!.toMutableList()
            cues.add(index, cue)
            _cues.value = cues
        }
    }

    fun setCue(index: Int, cue: Cue) {
        viewModelScope.launch {
            val cues = _cues.value!!.toMutableList()
            cues[index] = cue
            _cues.value = cues
        }
    }

    fun removeCue(index: Int) {
        viewModelScope.launch {
            val cues = _cues.value!!.toMutableList()
            cues.removeAt(index = index)
            _cues.value = cues
        }
    }
}

