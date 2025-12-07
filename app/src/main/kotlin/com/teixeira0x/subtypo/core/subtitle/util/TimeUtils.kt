package com.teixeira0x.subtypo.core.subtitle.util

import java.util.Locale

object TimeUtils {
    const val TIME_UNIT_HOUR = 3600000
    const val TIME_UNIT_MINUTE = 60000
    const val TIME_UNIT_SECOND = 1000

    /**
     * Converts milliseconds to a formatted time string.
     *
     * @param format The desired format: "hh:mm:ss,SSS", "mm:ss.SS", etc.
     * @return The formatted time string.
     */
    fun Long.getFormattedTime(format: String = "hh:mm:ss,SSS"): String {
        val hours = this / TIME_UNIT_HOUR
        val minutes = (this % TIME_UNIT_HOUR) / TIME_UNIT_MINUTE
        val seconds = (this % TIME_UNIT_MINUTE) / TIME_UNIT_SECOND
        val millis = this % TIME_UNIT_SECOND

        return when (format) {
            "h:mm:ss.cs" -> {
                val centis = millis / 10
                String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d.%02d",
                    hours,
                    minutes,
                    seconds,
                    centis
                )
            }

            "hh:mm:ss,SSS" -> String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d,%03d",
                hours,
                minutes,
                seconds,
                millis
            )

            "hh:mm:ss.SSS" -> String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d.%03d",
                hours,
                minutes,
                seconds,
                millis
            )

            "mm:ss.SS" -> String.format(
                Locale.getDefault(),
                "%02d:%02d.%02d",
                minutes,
                seconds,
                millis / 10
            )

            "hh:mm:ss" -> String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

            "mm:ss" -> String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }


    /**
     * Converts various time string formats to milliseconds.
     *
     * Supports formats like:
     * - hh:mm:ss,SSS (SubRip)
     * - mm:ss.SS (VTT)
     * - hh:mm:ss
     * - mm:ss
     *
     * @return Time in milliseconds.
     */
    fun String.getMilliseconds(): Long {
        val normalized = this.replace(',', '.')
        val parts = normalized.split(":")

        return when (parts.size) {
            3 -> { // hh:mm:ss(.SSS)
                val (hours, minutes, secondsMillis) = parts
                parseTime(hours, minutes, secondsMillis)
            }

            2 -> { // mm:ss(.SSS)
                val (minutes, secondsMillis) = parts
                parseTime("0", minutes, secondsMillis)
            }

            else -> throw IllegalArgumentException("Invalid time format: $this")
        }
    }

    private fun parseTime(hours: String, minutes: String, secondsMillis: String): Long {
        val secParts = secondsMillis.split(".")
        val seconds = secParts[0].toLong()
        val millis = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0

        return (hours.toLong() * TIME_UNIT_HOUR) +
                (minutes.toLong() * TIME_UNIT_MINUTE) +
                (seconds * TIME_UNIT_SECOND) +
                millis
    }

    fun parseAss(time: String): Long {
        val parts = time.split(":")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid ASS time format: $time")
        }
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        val secParts = parts[2].split(".")
        if (secParts.size != 2) {
            throw IllegalArgumentException("Invalid ASS time format: $time")
        }
        val seconds = secParts[0].toLong()
        val centiseconds = secParts[1].toLong()

        return (hours * TIME_UNIT_HOUR) +
                (minutes * TIME_UNIT_MINUTE) +
                (seconds * TIME_UNIT_SECOND) +
                (centiseconds * 10)
    }

    /**
     * Checks if the given time string matches the specified format.
     *
     * Supported formats:
     * - "hh:mm:ss,SSS"
     * - "mm:ss.SS"
     * - "hh:mm:ss"
     * - "mm:ss"
     *
     * @param time The time string to validate.
     * @param format The expected format ("hh:mm:ss,SSS", "mm:ss.SS", etc.).
     * @return `true` if the time matches the format, `false` otherwise.
     */
    fun isValidTime(time: String, format: String): Boolean {
        val normalized = time.replace(',', '.')
        val parts = normalized.split(":")

        return when (format) {
            "hh:mm:ss,SSS" ->
                parts.size == 3 &&
                        isInRange(parts[0], 0, 99, 2) &&
                        isInRange(parts[1], 0, 59, 2) &&
                        isValidSecondsFormat(parts[2], true)

            "hh:mm:ss.SSS" ->
                parts.size == 3 &&
                        isInRange(parts[0], 0, 99, 2) &&
                        isInRange(parts[1], 0, 59, 2) &&
                        isValidSecondsFormat(parts[2], true)

            "hh:mm:ss" ->
                parts.size == 3 &&
                        isInRange(parts[0], 0, 99, 2) &&
                        isInRange(parts[1], 0, 59, 2) &&
                        isInRange(parts[2], 0, 59, 2)

            "mm:ss.SS" ->
                parts.size == 2 &&
                        isInRange(parts[0], 0, 59, 2) &&
                        isValidSecondsFormat(parts[1], false)

            "mm:ss" ->
                parts.size == 2 && isInRange(parts[0], 0, 59, 2) && isInRange(parts[1], 0, 59, 2)

            else -> throw IllegalArgumentException("Unsupported format: $format")
        }
    }

    /**
     * Validates if the seconds part (ss or ss.SSS) is well-formed.
     *
     * @param secondsMillis The seconds part (e.g., "59.99" or "59,999").
     * @param millisecondsExpected If true, expects three-digit milliseconds.
     */
    private fun isValidSecondsFormat(
        secondsMillis: String,
        millisecondsExpected: Boolean,
    ): Boolean {
        val secParts = secondsMillis.split(".")
        return when {
            !millisecondsExpected && secParts.size == 1 -> isInRange(secParts[0], 0, 59, 2)
            millisecondsExpected && secParts.size == 2 ->
                isInRange(secParts[0], 0, 59, 2) && isInRange(secParts[1], 0, 999, 3)

            !millisecondsExpected && secParts.size == 2 ->
                isInRange(secParts[0], 0, 59, 2) && isInRange(secParts[1], 0, 99, 2)

            else -> false
        }
    }

    /**
     * Checks if the given String has the expected length and if its numeric value is within the
     * specified bounds.
     *
     * @param value The String to be checked.
     * @param min The minimum numeric value (inclusive).
     * @param max The maximum numeric value (inclusive).
     * @param expectedLength The expected length of the String.
     * @return `true` if the numeric value of the String is within the bounds and its length matches
     *   the expected length, `false` otherwise.
     */
    fun isInRange(value: String, min: Int, max: Int, expectedLength: Int): Boolean {
        return value.length == expectedLength &&
                value.toIntOrNull()?.let { it in min..max } ?: false
    }
}
