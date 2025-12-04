package com.teixeira0x.subtypo.ui.textlist.model

import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat

data class SimpleSubtitleData(
    val name: String,
    val format: SubtitleFormat,
    val extras: Map<String, String>? = null
)

