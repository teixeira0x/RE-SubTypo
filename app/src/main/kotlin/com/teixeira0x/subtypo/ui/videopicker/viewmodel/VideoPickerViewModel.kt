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

package com.teixeira0x.subtypo.ui.videopicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.media.model.Album
import com.teixeira0x.subtypo.core.media.model.Video
import com.teixeira0x.subtypo.core.media.usecase.GetAlbumsUseCase
import com.teixeira0x.subtypo.core.media.usecase.GetVideosUseCase
import com.teixeira0x.subtypo.core.resource.ResourcesManager
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerIntent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerUiEvent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerUiState
import com.teixeira0x.subtypo.ui.videopicker.util.videoListSortByName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPickerViewModel
@Inject
constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val resources: ResourcesManager,
) : ViewModel() {

    companion object {
        private val albumNameComparator = Comparator<Album> { a1, a2 -> a1.name.compareTo(a2.name) }

        private val videoNameComparator =
            Comparator<Video> { v1, v2 -> v1.displayName.compareTo(v2.displayName) }
    }

    private val _videoPickerUiState =
        MutableStateFlow<VideoPickerUiState>(VideoPickerUiState.Loading)
    val videoPickerUiState: StateFlow<VideoPickerUiState> = _videoPickerUiState.asStateFlow()

    private val _customUiEvent = MutableSharedFlow<VideoPickerUiEvent>()
    val customUiEvent: SharedFlow<VideoPickerUiEvent> = _customUiEvent.asSharedFlow()

    var currentAlbumId: String? = null
        private set

    fun doIntent(intent: VideoPickerIntent) {
        when (intent) {
            is VideoPickerIntent.Load -> loadAlbumsAndVideos()
            is VideoPickerIntent.LoadVideos -> loadVideos(intent)
        }
    }

    private fun loadAlbumsAndVideos() {
        _videoPickerUiState.value = VideoPickerUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val albums =
                getAlbumsUseCase().first().toMutableList().apply {
                    if (videoListSortByName) {
                        sortWith(albumNameComparator)
                    }

                    add(
                        0,
                        Album(id = "", name = resources.getString(R.string.video_picker_album_all)),
                    )
                }

            currentAlbumId =
                currentAlbumId.takeIf { id -> albums.any { it.id == id } }
                    ?: albums.firstOrNull()?.id

            var videos = getVideosUseCase(currentAlbumId).first()
            if (videoListSortByName) {
                videos = videos.sortedWith(videoNameComparator)
            }

            _videoPickerUiState.value =
                if (videos.isNotEmpty()) {
                    VideoPickerUiState.Loaded(albums, videos)
                } else {
                    VideoPickerUiState.Error(R.string.video_picker_error_no_videos)
                }
        }
    }

    private fun loadVideos(intent: VideoPickerIntent.LoadVideos) {
        viewModelScope.launch(Dispatchers.IO) {
            _customUiEvent.emit(VideoPickerUiEvent.ShowLoading)

            var videos = getVideosUseCase(intent.albumId).first()
            if (videoListSortByName) {
                videos = videos.sortedWith(videoNameComparator)
            }

            currentAlbumId = intent.albumId
            _customUiEvent.emit(VideoPickerUiEvent.UpdateVideoList(videos))

            _customUiEvent.emit(VideoPickerUiEvent.HideLoading)
        }
    }
}
