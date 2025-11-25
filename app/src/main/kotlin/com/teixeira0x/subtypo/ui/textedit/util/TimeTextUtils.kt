/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.textedit.util

import android.content.Context
import com.google.android.material.chip.Chip
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getMilliseconds
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.isValidTime

fun String.increaseTime(increaseMillis: Long, timeFormat: String): String {
    if (!isValidTime(this, timeFormat)) {
        return this
    }

    return (this.getMilliseconds() + increaseMillis).getFormattedTime(timeFormat)
}

fun String.decreaseTime(decreaseMillis: Long, timeFormat: String): String {
    if (!isValidTime(this, timeFormat)) {
        return this
    }

    val millis = this.getMilliseconds() - decreaseMillis
    return if (millis >= 0L) {
        millis.getFormattedTime(timeFormat)
    } else this
}

inline fun createTimeChip(context: Context, label: String, crossinline onClick: () -> Unit): Chip {
    return Chip(context).apply {
        text = label
        isClickable = true
        isCheckable = false
        setOnClickListener { onClick() }
    }
}
