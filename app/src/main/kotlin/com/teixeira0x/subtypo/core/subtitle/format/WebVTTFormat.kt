package com.teixeira0x.subtypo.core.subtitle.format

import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Diagnostic
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleData
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleParseResult
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getMilliseconds

class WebVTTFormat internal constructor() : SubtitleFormat("WebVTT", ".vtt") {

    override val isTimeBased: Boolean
        get() = true

    override val timeFormat: String
        get() = "hh:mm:ss.SSS"

    override fun toText(subtitle: Subtitle): String {
        val cues = subtitle.data.cues
        val sb = StringBuilder()

        cues.forEach {
            with(it) {
                sb.append(startTime.getFormattedTime(timeFormat)).append(" --> ")
                    .append(endTime.getFormattedTime(timeFormat)).append("\n").append(text)
                    .append("\n\n")
            }
        }
        return sb.toString().trim()
    }

    override fun parseText(text: String): SubtitleParseResult {
        val cues = mutableListOf<Cue>()
        val diagnostics = mutableListOf<Diagnostic>()

        val lines = text.trim().lines()
        var index = 0

        while (index < lines.size) {
            val line = lines[index].trim()
            if (line.isEmpty()) {
                index++
                continue
            }

            //index++
            if (index >= lines.size) {
                diagnostics.add(
                    Diagnostic(
                        kind = Diagnostic.Kind.ERROR,
                        lineNumber = index + 1,
                        message = "Missing timecode",
                    )
                )
                break
            }

            val times = parseTimeCode(index, lines[index])
            if (times.isEmpty()) {
                diagnostics.add(
                    Diagnostic(
                        kind = Diagnostic.Kind.ERROR,
                        line = lines[index],
                        lineNumber = index + 1,
                        message = "Invalid timecode format.",
                    )
                )

                // Skip the next few lines of non-empty text as it is time to move on to
                // the next cue.
                while (index < lines.size && lines[index].trim().isNotEmpty()) {
                    index++
                }
                continue
            }

            if (times[0] >= times[1]) {
                diagnostics.add(
                    Diagnostic(
                        kind = Diagnostic.Kind.WARNING,
                        line = lines[index],
                        lineNumber = index + 1,
                        message = "The start time cannot be greater than or equal to the end time.",
                    )
                )
            }

            index++
            val textBuilder = StringBuilder()
            while (index < lines.size && lines[index].trim().isNotEmpty()) {
                textBuilder.append(lines[index]).append("\n")
                index++
            }

            cues.add(
                Cue(startTime = times[0], endTime = times[1], text = textBuilder.toString().trim())
            )
        }

        return SubtitleParseResult(diagnostics = diagnostics, data = SubtitleData(cues))
    }

    private fun parseTimeCode(index: Int, timeCodeLine: String): List<Long> {
        val timeCodes = timeCodeLine.split(" --> ")
        if (timeCodes.size != 2 ||
            !isValidVTT(timeCodes[0]) ||
            !isValidVTT(timeCodes[1])
        ) {
            return emptyList()
        }

        return timeCodes.map { it.getMilliseconds() }
    }

    private fun isValidVTT(time: String): Boolean {
        return Regex("""^(\d{2}:)?\d{2}:\d{2}\.\d{3}$""").matches(time)
    }


}
