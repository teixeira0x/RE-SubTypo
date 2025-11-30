package com.teixeira0x.subtypo.core.subtitle.format

import com.teixeira0x.subtypo.core.subtitle.exception.UnknownFormatException
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleParseResult
import com.teixeira0x.subtypo.core.subtitle.util.containsErrors

abstract class SubtitleFormat(val name: String, val extension: String) {

    companion object {
        val allSubtitleFormats =
            arrayOf<SubtitleFormat>(SubRipFormat(), LRCLyricsFormat(), WebVTTFormat())

        fun of(id: Int): SubtitleFormat {
            return allSubtitleFormats[id]
        }

        fun of(extension: String): SubtitleFormat {
            return allSubtitleFormats.find { it.extension == extension }
                ?: throw UnknownFormatException("The `$extension` format is not supported")
        }

        suspend fun of(extension: String, text: String): Pair<SubtitleFormat, SubtitleParseResult> {
            for (subtitleFormat in allSubtitleFormats) {
                if (subtitleFormat.extension == extension) {
                    return subtitleFormat to subtitleFormat.parseText(text)
                }
            }

            for (subtitleFormat in allSubtitleFormats) {
                if (subtitleFormat.extension != extension) {
                    val parseResult = subtitleFormat.parseText(text)
                    if (!parseResult.diagnostics.containsErrors()) {
                        return subtitleFormat to parseResult
                    }
                }
            }

            throw UnknownFormatException("The `$extension` format is not supported")
        }

        fun formatId(format: SubtitleFormat): Int {
            return allSubtitleFormats.indexOf(format)
        }
    }

    abstract val isTimeBased: Boolean

    abstract val timeFormat: String

    abstract fun toText(subtitle: Subtitle): String

    abstract fun parseText(text: String): SubtitleParseResult
}
