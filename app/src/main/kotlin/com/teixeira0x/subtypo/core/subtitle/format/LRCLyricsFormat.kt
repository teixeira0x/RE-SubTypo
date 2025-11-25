package com.teixeira0x.subtypo.core.subtitle.format

import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Diagnostic
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleData
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleParseResult
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime

class LRCLyricsFormat internal constructor() : SubtitleFormat("LRC Lyrics", ".lrc") {

    private val timeRegex = """\[(\d{2}):(\d{2})\.(\d{2})\]""".toRegex()

    override val isTimeBased: Boolean
        get() = true

    override val timeFormat: String
        get() = "mm:ss.SS"

    override fun toText(subtitle: Subtitle): String {
        val cues = subtitle.data.cues
        val sb = StringBuilder()
        for (i in cues.indices) {
            with(cues[i]) {
                sb.append("[")
                    .append(startTime.getFormattedTime(timeFormat))
                    .append("] ")
                    .append(text)
                    .append("\n")
            }
        }
        return sb.toString().trim()
    }

    override fun parseText(text: String): SubtitleParseResult {
        val cues = mutableListOf<Cue>()
        val diagnostics = mutableListOf<Diagnostic>()

        val lines = text.trim().lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                i++
                continue
            }

            val timeMatch = timeRegex.find(line)
            val startTime = parseTimeCode(line)
            if (timeMatch == null || startTime == null) {
                diagnostics.add(
                    Diagnostic(
                        kind = Diagnostic.Kind.ERROR,
                        line = line,
                        lineNumber = i + 1,
                        message = "Invalid time format. Expected format: [mm:ss.SS]",
                    )
                )
                i++
                continue
            }

            val text = line.substring(timeMatch.range.last + 1).trim()

            var endTime = startTime + 1000L
            var nextIndex = i + 1
            while (nextIndex < lines.size) {
                val nextLine = lines[nextIndex].trim()

                if (nextLine.isNotEmpty()) {
                    val nextStartTime = parseTimeCode(nextLine)
                    if (nextStartTime != null) {
                        endTime = nextStartTime
                        break
                    }
                }
                nextIndex++
            }

            if (endTime <= startTime) {
                diagnostics.add(
                    Diagnostic(
                        kind = Diagnostic.Kind.WARNING,
                        line = line,
                        lineNumber = i + 1,
                        message = "The start time cannot be greater than or equal to the end time.",
                    )
                )
            }

            cues.add(Cue(startTime = startTime, endTime = endTime, text = text))

            i++
        }

        return SubtitleParseResult(diagnostics = diagnostics, data = SubtitleData(cues))
    }

    private fun parseTimeCode(line: String): Long? {
        val match = timeRegex.find(line) ?: return null
        val (minutes, seconds, centiseconds) = match.destructured

        return (minutes.toLong() * 60 * 1000) +
            (seconds.toLong() * 1000) +
            (centiseconds.toLong() * 10)
    }
}
