package com.teixeira0x.subtypo.ui.sourceview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle

class SourceViewModel : ViewModel() {
    private val _subtitleData = MutableLiveData(Subtitle("", SubtitleFormat.of(".srt")))

    val subtitle: LiveData<Subtitle>
        get() = _subtitleData

    fun updateSubtitle(subtitle: Subtitle) {
        _subtitleData.value = subtitle
    }

    fun setSubtitleFormat(subtitleFormat: SubtitleFormat) {
        _subtitleData.value = _subtitleData.value!!.copy(format = subtitleFormat)
    }

}