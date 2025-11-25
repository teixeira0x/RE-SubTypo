package com.teixeira0x.subtypo.core.subtitle.model

import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat

data class Subtitle(
    val name: String,
    val format: SubtitleFormat = SubtitleFormat.of(".srt"),
    val data: SubtitleData = SubtitleData(emptyList()),
) {

    val fullName: String
        get() =
            if (name.endsWith(format.extension)) {
                name
            } else {
                name + format.extension
            }

    fun toText(): String = format.toText(this)
}
