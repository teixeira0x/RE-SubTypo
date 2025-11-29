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

import android.content.Context
import android.util.Pair
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer.DecoderInitializationException
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil.DecoderQueryException
import com.teixeira0x.subtypo.R

class PlayerErrorMessageProvider(private val context: Context) :
    ErrorMessageProvider<PlaybackException> {

    override fun getErrorMessage(e: PlaybackException): Pair<Int, String> {
        var errorString = context.getString(R.string.video_player_error_generic)
        val cause = e.cause

        if (cause is DecoderInitializationException) {
            val codecInfo = cause.codecInfo
            errorString =
                if (codecInfo == null) {
                    // Special case for decoder initialization failures.
                    when {
                        cause.cause is DecoderQueryException -> {
                            context.getString(R.string.video_player_error_querying_decoders)
                        }

                        cause.secureDecoderRequired -> {
                            context.getString(
                                R.string.video_player_error_no_secure_decoder,
                                cause.mimeType,
                            )
                        }

                        else -> {
                            context.getString(
                                R.string.video_player_error_no_decoder,
                                cause.mimeType,
                            )
                        }
                    }
                } else {
                    context.getString(
                        R.string.video_player_error_instantiating_decoder,
                        codecInfo.name,
                    )
                }
        }

        return Pair.create(0, errorString)
    }
}
