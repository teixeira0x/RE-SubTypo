package com.teixeira0x.subtypo.ui.videoplayer.waveform

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max

suspend fun generateWaveformCache(
    filePath: String,
    cacheFile: File,
    windowMs: Int = 20
): WaveformMeta = withContext(Dispatchers.IO) {
    val metaFile = File(cacheFile.parentFile, cacheFile.name + ".meta")
    if (cacheFile.exists() && metaFile.exists()) {
        val parts = metaFile.readText().split(",")
        if (parts.size == 3) {
            return@withContext WaveformMeta(
                durationMs = parts[0].toLong(),
                windowMs = parts[1].toInt(),
                points = parts[2].toInt()
            )
        }
    }
    val extractor = MediaExtractor()
    extractor.setDataSource(filePath)

    val audioTrack = (0 until extractor.trackCount).firstOrNull { i ->
        extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
    } ?: throw IllegalStateException("No audio track")

    extractor.selectTrack(audioTrack)
    val format = extractor.getTrackFormat(audioTrack)
    val mime = format.getString(MediaFormat.KEY_MIME)!!

    val durationUs = try {
        format.getLong(MediaFormat.KEY_DURATION)
    } catch (e: Exception) {
        // fallback if missing
        -1L
    }

    val codec = MediaCodec.createDecoderByType(mime)
    codec.configure(format, null, null, 0)
    codec.start()

    val inputBuffers = codec.inputBuffers
    val outputBuffers = codec.outputBuffers
    val info = MediaCodec.BufferInfo()

    val fos = FileOutputStream(cacheFile)
    val windowUs = windowMs * 1000L

    var currentWindowEndUs = 0L
    var currentMax = 0f
    var sawInputEOS = false
    var sawOutputEOS = false

    // We will compute approx start as first presentationTimeUs
    var firstPtsUs = -1L
    var lastPtsUs = 0L
    var totalPoints = 0

    while (!sawOutputEOS) {
        if (!sawInputEOS) {
            val inIndex = codec.dequeueInputBuffer(20_000)
            if (inIndex >= 0) {
                val ib = inputBuffers[inIndex]
                val sampleSize = extractor.readSampleData(ib, 0)
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    sawInputEOS = true
                } else {
                    val pts = extractor.sampleTime
                    if (firstPtsUs < 0) firstPtsUs = pts
                    codec.queueInputBuffer(inIndex, 0, sampleSize, pts, 0)
                    extractor.advance()
                }
            }
        }

        val outIndex = codec.dequeueOutputBuffer(info, 20_000)
        if (outIndex >= 0) {
            val ob = outputBuffers[outIndex]
            ob.position(info.offset)
            ob.limit(info.offset + info.size)

            // iterate PCM 16-bit (common)
            while (ob.remaining() >= 2) {
                val low = ob.get().toInt() and 0xFF
                val high = ob.get().toInt()
                val s = ((high shl 8) or low).toShort().toFloat() / Short.MAX_VALUE
                val amp = abs(s)
                currentMax = max(currentMax, amp)
            }

            val ptsUs = if (info.presentationTimeUs >= 0) info.presentationTimeUs else lastPtsUs
            lastPtsUs = ptsUs

            // initialize currentWindowEndUs on first frame
            if (currentWindowEndUs == 0L) {
                currentWindowEndUs = firstPtsUs + windowUs
            }

            // flush windows while pts >= end
            while (ptsUs >= currentWindowEndUs) {
                // write currentMax as float32 (4 bytes)
                val bb = ByteBuffer.allocateDirect(4).order(ByteOrder.BIG_ENDIAN)
                bb.putFloat(currentMax)
                bb.flip()
                fos.channel.write(bb)

                totalPoints++
                currentMax = 0f
                currentWindowEndUs += windowUs
            }

            codec.releaseOutputBuffer(outIndex, false)

            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                // write last pending windows (if any)
                // write final currentMax (even if zero) then stop
                val bb = ByteBuffer.allocateDirect(4).order(ByteOrder.BIG_ENDIAN)
                bb.putFloat(currentMax)
                bb.flip()
                fos.channel.write(bb)

                totalPoints++
                sawOutputEOS = true
            }
        }
    }

    fos.flush()
    fos.close()
    codec.stop()
    codec.release()
    extractor.release()

    val durationMs = if (durationUs > 0) durationUs / 1000L else (lastPtsUs - firstPtsUs) / 1000L
    val meta = WaveformMeta(durationMs = durationMs, windowMs = windowMs, points = totalPoints)
    // write accompanying .meta file

    metaFile.writeText("${meta.durationMs},${meta.windowMs},${meta.points}")
    return@withContext meta
}

data class WaveformMeta(val durationMs: Long, val windowMs: Int, val points: Int)
