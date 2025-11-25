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

package com.teixeira0x.subtypo.ui.videoplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.ui.videoplayer.model.ExoCuesTimed
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerIntent
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerUiEvent
import com.teixeira0x.subtypo.ui.videoplayer.util.SubtitleUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VideoPlayerViewModel : ViewModel() {
    private val _customUiEvent = MutableSharedFlow<VideoPlayerUiEvent>()

    val customUiEvent: SharedFlow<VideoPlayerUiEvent> = _customUiEvent.asSharedFlow()

    private val _videoPath = MutableLiveData<String>("")

    val videoPath: LiveData<String>
        get() = _videoPath

    private val _playerPosition = MutableLiveData(0L)
    val playerPosition: LiveData<Long>
        get() = _playerPosition

    var subtitle: Subtitle? = null
        private set

    var currentExoCuesTimed: List<ExoCuesTimed>? = null
        private set

    var isPlayerVisible: Boolean = true
        private set

    fun doEvent(event: VideoPlayerIntent) {
        viewModelScope.launch {
            when (event) {
                is VideoPlayerIntent.LoadVideoUri -> loadVideo(event.videoUri)
                is VideoPlayerIntent.SeekTo -> _customUiEvent.emit(VideoPlayerUiEvent.SeekTo(event.position))
                is VideoPlayerIntent.Pause -> _customUiEvent.emit(VideoPlayerUiEvent.Pause)
                is VideoPlayerIntent.Play -> _customUiEvent.emit(VideoPlayerUiEvent.Play)
            }
        }
    }

    private fun loadVideo(videoUri: String) {
        viewModelScope.launch {
            _videoPath.postValue(videoUri)
            _customUiEvent.emit(VideoPlayerUiEvent.LoadUri(videoUri))
        }
    }

    fun setSubtitle(subtitle: Subtitle?) {
        viewModelScope.launch {
            this@VideoPlayerViewModel.subtitle = subtitle
            this@VideoPlayerViewModel.currentExoCuesTimed = SubtitleUtils.getExoCuesTimed(subtitle)

            _customUiEvent.emit(VideoPlayerUiEvent.LoadSubtitle(subtitle))
        }
    }

    fun setPlayerVisibility(visible: Boolean) {
        viewModelScope.launch {
            this@VideoPlayerViewModel.isPlayerVisible = visible
            _customUiEvent.emit(VideoPlayerUiEvent.Visibility(visible))
        }
    }

    fun updatePlayerPosition(position: Long) {
        _playerPosition.value = position
    }
}
