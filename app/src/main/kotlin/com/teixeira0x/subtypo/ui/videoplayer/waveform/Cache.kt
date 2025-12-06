package com.teixeira0x.subtypo.ui.videoplayer.waveform

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

class WaveformCache(private val cacheFile: File) {
    private var raf: RandomAccessFile? = null
    private var channel: FileChannel? = null
    private var mapped: MappedByteBuffer? = null
    var meta: WaveformMeta? = null
        private set

    fun open() {
        if (!cacheFile.exists()) throw IllegalStateException("Cache missing")
        raf = RandomAccessFile(cacheFile, "r")
        channel = raf!!.channel
        // map whole file (safe because file is small; if huge, map ranges)
        mapped =
            channel!!.map(FileChannel.MapMode.READ_ONLY, 0, channel!!.size()) as MappedByteBuffer
        // read meta
        val metaFile = File(cacheFile.parentFile, cacheFile.name + ".meta")
        if (metaFile.exists()) {
            val parts = metaFile.readText().split(",")
            meta = WaveformMeta(parts[0].toLong(), parts[1].toInt(), parts[2].toInt())
        }
    }

    fun close() {
        // try to free mapped buffer (no official unmap API; let GC)
        mapped = null
        channel?.close()
        raf?.close()
    }


    fun readRange(startIndex: Int, endIndex: Int): FloatArray {
        val m = mapped ?: throw IllegalStateException("Cache not opened")
        val metaPoints = meta?.points ?: (m.limit() / 4)
        // clamp indices to valid range
        val s = startIndex.coerceIn(0, metaPoints)
        var e = endIndex.coerceIn(0, metaPoints)
        if (e < s) e = s
        val count = e - s
        if (count == 0) return FloatArray(0)

        val bytePos = s.toLong() * 4L
        val bytesNeeded = count.toLong() * 4L
        val capacity = m.capacity().toLong()

        // Safety: if requested range goes beyond mapped buffer, clamp it
        if (bytePos >= capacity) return FloatArray(0)
        val availableBytes = capacity - bytePos
        val usableBytes = min(availableBytes, bytesNeeded)
        val usableCount = (usableBytes / 4L).toInt()
        if (usableCount <= 0) return FloatArray(0)

        // Duplicate buffer and set position/limit in bytes
        val dup = m.duplicate()
        dup.order(ByteOrder.BIG_ENDIAN) // must match how you wrote the floats
        dup.position(bytePos.toInt())
        dup.limit((bytePos + usableCount * 4).toInt())

        // Use asFloatBuffer for fast float reads
        val floatBuf = dup.asFloatBuffer()
        val out = FloatArray(usableCount)
        floatBuf.get(out)

        return out
    }
}

