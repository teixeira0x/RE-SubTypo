package com.teixeira0x.subtypo.ui.videoplayer.mvi

sealed class VideoPlayerIntent {

    data object SelectVideo : VideoPlayerIntent()

    data class SeekTo(val position: Long) : VideoPlayerIntent()

    data object Pause : VideoPlayerIntent()

    data object Play : VideoPlayerIntent()

}