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

package com.teixeira0x.subtypo.ui.videoplayer.util

import androidx.annotation.OptIn
import androidx.core.text.HtmlCompat
import androidx.media3.common.util.UnstableApi
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.ui.videoplayer.model.ExoCuesTimed
import androidx.media3.common.text.Cue as ExoCue

object SubtitleUtils {

    @OptIn(UnstableApi::class)
    suspend fun getExoCuesTimed(cues: List<Cue>?): List<ExoCuesTimed>? {
        if (cues == null || cues.isEmpty()) {
            return null
        }

        val groupedCues = cues.groupBy { it.startTime to it.endTime }

        return groupedCues.map { (time, cuesGroup) ->
            val (startTime, endTime) = time

            val exoCues =
                cuesGroup.map { cue ->
                    val text = HtmlCompat.fromHtml(cue.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    ExoCue.Builder().setText(text).build()
                }

            ExoCuesTimed(exoCues = exoCues, startTime = startTime, endTime = endTime)
        }
    }
}
