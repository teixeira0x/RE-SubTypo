package com.teixeira0x.subtypo.core.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.Scroller
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.core.subtitle.model.Cue

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f
    }

    private val scroller = Scroller(context)

    private var scrollXPos = 0f
    private var zoom = 2f

    private var durationMs = 0L
    private var positionMs = 0L

    private var cues: List<Cue> = emptyList()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawCues(canvas)
        drawTimelineTicks(canvas)
        drawPlayhead(canvas)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            scrollXPos = scroller.currX.toFloat()
            invalidate()
        }
    }

    fun setDuration(ms: Long) {
        durationMs = ms
        invalidate()
    }

    fun setPosition(ms: Long) {
        positionMs = ms
        invalidate()
    }

    fun setSubtitles(list: List<Cue>) {
        cues = list
        invalidate()
    }

    // ---------------------------------------------------
    // DRAW TIMELINE TICKS (seconds/minutes)
    // ---------------------------------------------------
    private fun drawTimelineTicks(canvas: Canvas) {
        if (durationMs <= 0) return

        val width = canvas.width
        val height = canvas.height

        val colorSecondaryVariant =
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryVariant)

        paint.color = colorSecondaryVariant

        val totalSeconds = (durationMs / 1000).toInt()

        for (i in 0..totalSeconds) {

            val x = ((i.toFloat() / totalSeconds) * width) * zoom - scrollXPos

            paint.alpha = 100

            val lineTop = when {
                i % 3600 == 0 -> height * 0.25f   // hours
                i % 60 == 0 -> height * 0.50f     // minutes
                else -> height * 0.75f            // seconds
            }

            canvas.drawLine(x, lineTop, x, height.toFloat(), paint)
        }
    }

    // ---------------------------------------------------
    // DRAW PLAYHEAD (always centered)
    // ---------------------------------------------------
    private fun drawPlayhead(canvas: Canvas) {
        val width = canvas.width
        val height = canvas.height

        val colorControlNormal =
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorControlNormal)

        paint.color = colorControlNormal

        val centerX = width / 2f

        // vertical line
        canvas.drawLine(centerX, 0f, centerX, height - (height * 0.25f), paint)

        // small triangle
        val size = 8f
        val path = Path().apply {
            moveTo(centerX, size)
            lineTo(centerX - size, 0f)
            lineTo(centerX + size, 0f)
            close()
        }
        canvas.drawPath(path, paint)

        // update scroll based on position
        updateScroll(width)
    }

    private fun updateScroll(width: Int) {
        if (durationMs <= 0) return

        val targetScroll =
            ((positionMs.toFloat() / durationMs) * width * zoom) - (width / 2f)

        scroller.startScroll(scroller.currX, 0, (targetScroll - scroller.currX).toInt(), 0, 200)
        invalidate()
    }

    // ---------------------------------------------------
    // DRAW SUBTITLES
    // ---------------------------------------------------
    private fun drawCues(canvas: Canvas) {
        if (durationMs <= 0) return

        val width = canvas.width
        val height = canvas.height

        val colorSub =
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse)

        paint.color = colorSub

        for (cue in cues) {
            val left = ((cue.startTime / durationMs.toFloat()) * width) * zoom - scrollXPos
            val right = ((cue.endTime / durationMs.toFloat()) * width) * zoom - scrollXPos

            canvas.drawRect(RectF(left, 0f, right, height.toFloat()), paint)
        }
    }
}


