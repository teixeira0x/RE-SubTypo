package com.teixeira0x.subtypo.core.subtitle.format

import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Diagnostic
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleData
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleParseResult
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime

class AdvancedSubStationAlphaFormat : SubtitleFormat("Advanced SubStation Alpha", ".ass") {
    override val isTimeBased: Boolean
        get() = true
    override val timeFormat: String
        get() = "h:mm:ss.cs"

    override fun toText(subtitle: Subtitle): String {
        val builder = StringBuilder()
        val extras = subtitle.data.extras?.mapValues { it.value } ?: emptyMap()

        // Write sections from extras, with a fallback to default
        val scriptInfo = extras["[Script Info]"]
        if (!scriptInfo.isNullOrBlank()) {
            builder.appendLine("[Script Info]")
            builder.appendLine(scriptInfo)
        } else {
            builder.appendLine("[Script Info]")
            builder.appendLine("Title: SubTypo Generated Subtitle")
            builder.appendLine("ScriptType: v4.00+")
            builder.appendLine("WrapStyle: 0")
            builder.appendLine("PlayResX: 1280")
            builder.appendLine("PlayResY: 720")
        }
        builder.appendLine()

        val styles = extras["[V4+ Styles]"]
        if (!styles.isNullOrBlank()) {
            builder.appendLine("[V4+ Styles]")
            builder.appendLine(styles)
        } else {
            builder.appendLine("[V4+ Styles]")
            builder.appendLine("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding")
            builder.appendLine("Style: Default,Arial,20,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,2,10,10,10,1")
        }
        builder.appendLine()

        // Handle other sections that are not Events
        extras.filter { it.key != "[Script Info]" && it.key != "[V4+ Styles]" && it.key != "[Events]" }
            .forEach { (key, value) ->
                builder.appendLine(key)
                builder.appendLine(value)
                builder.appendLine()
            }

        builder.appendLine("[Events]")
        val eventsContent = extras["[Events]"]
        val formatLine = eventsContent?.lines()?.find { it.startsWith("Format:") }
            ?: "Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text"
        val formatOrder = formatLine.substringAfter("Format:").split(',').map { it.trim() }

        builder.appendLine(formatLine)

        // Write non-dialogue lines from original events section
        eventsContent?.lines()?.forEach { line ->
            if (!line.startsWith("Dialogue:") && !line.startsWith("Format:")) {
                builder.appendLine(line)
            }
        }

        subtitle.data.cues.forEach { cue ->
            val start = cue.startTime.getFormattedTime(timeFormat)
            val end = cue.endTime.getFormattedTime(timeFormat)
            // ASS uses \N for newlines in dialogues.
            val text = cue.text.replace("\n", "\\N")

            val dialogueValues = formatOrder.map { key ->
                when (key) {
                    "Start" -> start
                    "End" -> end
                    "Text" -> text
                    else -> cue.extras[key] ?: ""
                }
            }
            builder.appendLine("Dialogue: ${dialogueValues.joinToString(",")}")
        }

        return builder.toString()
    }

    override fun parseText(text: String): SubtitleParseResult {
        val diagnostics = mutableListOf<Diagnostic>()
        val cues = mutableListOf<Cue>()
        val sections = mutableMapOf<String, MutableList<String>>()
        var currentSection = ""
        var eventsFormat = listOf<String>()

        text.lines().forEachIndexed { index, line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                currentSection = trimmedLine
                sections[currentSection] = mutableListOf()
                return@forEachIndexed
            }

            if (currentSection.isNotBlank()) {
                // Don't add dialogue lines to the raw section text, they are generated from cues
                if (!(currentSection == "[Events]" && trimmedLine.startsWith("Dialogue:"))) {
                    sections[currentSection]?.add(line)
                }
            }

            if (currentSection == "[Events]") {
                when {
                    trimmedLine.startsWith("Format:") -> {
                        eventsFormat =
                            trimmedLine.substringAfter("Format:").split(',').map { it.trim() }
                    }

                    trimmedLine.startsWith("Dialogue:") -> {
                        if (eventsFormat.isEmpty()) {
                            diagnostics.add(
                                Diagnostic(
                                    Diagnostic.Kind.ERROR,
                                    trimmedLine,
                                    index + 1,
                                    "Format line in [Events] section not found or is before Dialogue line.",
                                )
                            )
                            return@forEachIndexed
                        }

                        // The text field is the last one and can contain commas.
                        val dialogueParts = trimmedLine.substringAfter("Dialogue:")
                            .split(',', limit = eventsFormat.size)
                        val values = eventsFormat.zip(dialogueParts).toMap().toMutableMap()

                        val start =
                            values.remove("Start")?.let { TimeUtils.parseAss(it.trim()) } ?: 0L
                        val end = values.remove("End")?.let { TimeUtils.parseAss(it.trim()) } ?: 0L
                        // ASS uses \N for newlines.
                        val textContent = values.remove("Text")?.replace("\\N", "\n") ?: ""

                        cues.add(Cue(start, end, textContent, values))
                    }
                }
            }
        }

        val extras = sections.mapValues { it.value.joinToString("\n") }

        return SubtitleParseResult(diagnostics, SubtitleData(cues, extras))
    }
}
