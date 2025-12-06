package com.teixeira0x.subtypo.core.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.ui.videoplayer.waveform.WaveformCache
import com.teixeira0x.subtypo.ui.videoplayer.waveform.WaveformMeta
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // PAINTS
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2B2B2B.toInt() }
    private val backWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF888888.toInt() }

    private val cuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x55FFEB3B
        style = Paint.Style.FILL
    }

    private val playheadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3f
    }

    // DATA
    var cache: WaveformCache? = null
    var meta: WaveformMeta? = null

    var totalDurationMs: Long = 0L
    var viewStartMs: Long = 0L
    var viewEndMs: Long = 10_000L

    private var cues: List<Cue> = emptyList()
    private var playheadMs: Long = 0L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val meta = meta ?: return
        val cache = cache ?: return

        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val windowMs = meta.windowMs
        val totalPoints = meta.points

        // ------------------------------------------
        // WAVEFORM
        // ------------------------------------------
        val startIndex = max(0, floor(viewStartMs.toDouble() / windowMs).toInt())
        val endIndex = min(totalPoints, ceil(viewEndMs.toDouble() / windowMs).toInt())
        if (endIndex <= startIndex) return

        val pointsToDraw = endIndex - startIndex
        val pixels = width
        val step = max(1, (pointsToDraw / pixels.toDouble()).toInt())

        val data = cache.readRange(startIndex, endIndex)

        val centerY = h / 2f
        val barWidth = width.toFloat() / (pointsToDraw / step)
        var x = 0f

        var i = 0
        while (i < data.size) {

            var maxV = 0f
            var j = 0
            while (j < step && i + j < data.size) {
                maxV = max(maxV, data[i + j])
                j++
            }

            val heightPx = maxV * h
            val left = x
            val right = x + barWidth
            val top = centerY - heightPx / 2
            val bottom = centerY + heightPx / 2

            canvas.drawRect(left, top, right, bottom, backWavePaint)

            x += barWidth
            i += step
        }

        // ------------------------------------------
        // CUES
        // ------------------------------------------
        drawCues(canvas)

        // ------------------------------------------
        // PLAYHEAD
        // ------------------------------------------
        drawPlayhead(canvas)
    }

    private fun drawCues(canvas: Canvas) {
        if (totalDurationMs == 0L) return

        val zoomFactor = width / (viewEndMs - viewStartMs).toFloat()

        for (cue in cues) {
            val left = (cue.startTime - viewStartMs) * zoomFactor
            val right = (cue.endTime - viewStartMs) * zoomFactor

            if (right < 0 || left > width) continue

            canvas.drawRect(left, 0f, right, height.toFloat(), cuePaint)
        }
    }

    private fun drawPlayhead(canvas: Canvas) {
        val x = ((playheadMs - viewStartMs).toFloat() / (viewEndMs - viewStartMs)) * width

        canvas.drawLine(x, 0f, x, height.toFloat(), playheadPaint)
    }

    private fun formatMs(ms: Long): String {
        val sec = ms / 1000
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    fun setViewWindow(startMs: Long, endMs: Long) {
        viewStartMs = startMs
        viewEndMs = endMs
        invalidate()
    }

    fun setPlayhead(ms: Long) {
        playheadMs = ms
        invalidate()
    }

    fun setCues(list: List<Cue>) {
        cues = list
        invalidate()
    }
}