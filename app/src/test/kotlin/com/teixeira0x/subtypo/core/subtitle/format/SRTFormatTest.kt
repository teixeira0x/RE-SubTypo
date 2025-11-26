package com.teixeira0x.subtypo.core.subtitle.format

import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleData
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleParseResult
import org.junit.Assert.assertEquals
import org.junit.Test

class SRTFormatTest {

    private val srtFormat = SubRipFormat()

    @Test
    fun `test toText with Cue list`() {
        val cues =
            listOf<Cue>(
                Cue(
                    startTime = 10000L, // 10000ms -> 00:10.00
                    endTime = 20000L, // 20000ms -> 00:20.00
                    text = "First line",
                ),
                Cue(
                    startTime = 20000L, // 20000ms -> 00:20.00
                    endTime = 30000L, // 30000ms -> 00:30.00
                    text = "Second line",
                ),
                Cue(
                    startTime = 30000L, // 30000ms -> 00:30.00
                    endTime = 40000L, // 40000ms -> 00:40.00
                    text = "Third line",
                ),
            )

        val text =
            srtFormat.toText(
                Subtitle(name = "Test", data = SubtitleData(cues = cues))
            )

        assertEquals(
            """
      1
      00:00:10,000 --> 00:00:20,000
      First line
      
      2
      00:00:20,000 --> 00:00:30,000
      Second line

      3
      00:00:30,000 --> 00:00:40,000
      Third line
      """
                .trimIndent(),
            text,
        )
    }


    @Test
    fun `test parseText with valid LRC input`() {
        val input =
            """
      1
      00:00:10,000 --> 00:00:20,000
      First line
      
      2
      00:00:20,000 --> 00:00:30,000
      Second line

      3
      00:00:30,000 --> 00:00:31,000
      Third line
        """
                .trimIndent()

        val result: SubtitleParseResult = srtFormat.parseText(input)
        val cues = result.data.cues
        val diagnostics = result.diagnostics

        assertEquals(3, cues.size)
        assertEquals(0, diagnostics.size)

        assertEquals(10000L, cues[0].startTime) // 00:10.00 -> 10000ms
        assertEquals(20000L, cues[0].endTime) // 00:20.00 -> 20000ms

        assertEquals(20000L, cues[1].startTime) // 00:20.00 -> 20000ms
        assertEquals(30000L, cues[1].endTime) // 00:30.00 -> 30000ms

        assertEquals(30000L, cues[2].startTime) // 00:30.00 -> 30000ms
        assertEquals(31000L, cues[2].endTime) // Duração padrão de 1 segundo
    }

    @Test
    fun `test parseText with empty lines`() {
        val input =
            """
      1
      00:00:10,000 --> 00:00:30,000
      First line

      
      
      
      2
      00:00:30,000 --> 00:00:40,000
      Second line




      3
      00:00:40,000 --> 00:00:41,000
      Third line
        """
                .trimIndent()

        val result: SubtitleParseResult = srtFormat.parseText(input)
        val cues = result.data.cues
        val diagnostics = result.diagnostics

        assertEquals(3, cues.size)
        assertEquals(0, diagnostics.size)

        assertEquals(10000L, cues[0].startTime) // 00:10.00 -> 10000ms
        assertEquals(30000L, cues[0].endTime) // 00:30.00 -> 30000ms

        assertEquals(30000L, cues[1].startTime) // 00:30.00 -> 30000ms
        assertEquals(40000L, cues[1].endTime) // 00:40.00 -> 40000ms

        assertEquals(40000L, cues[2].startTime) // 00:40.00 -> 40000ms
        assertEquals(41000L, cues[2].endTime) // 00:41.00 -> 41000ms
    }

    @Test
    fun `test parseText with invalid times`() {
        val input =
            """
      1
      00:00:10,000 --> 00:00:30,000
      First line
      
      2
      00:00:20000 --> 00:00:20,000
      Erroorrrr line

      3
      00:00:30,000 --> 00:00:31,000
      Third line
        """
                .trimIndent()

        val result: SubtitleParseResult = srtFormat.parseText(input)
        val cues = result.data.cues
        val diagnostics = result.diagnostics

        assertEquals(2, cues.size)
        assertEquals(1, diagnostics.size)

        assertEquals(10000L, cues[0].startTime) // 00:10.00 -> 10000ms
        assertEquals(30000L, cues[0].endTime) // 00:30.00 -> 30000ms

        assertEquals(30000L, cues[1].startTime) // 00:30.00 -> 30000ms
        assertEquals(31000L, cues[1].endTime) // 00:31.00 -> 31000ms
    }

}