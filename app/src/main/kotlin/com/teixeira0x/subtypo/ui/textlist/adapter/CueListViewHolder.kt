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

package com.teixeira0x.subtypo.ui.textlist.adapter

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.databinding.LayoutCueItemBinding

class CueListViewHolder(
    private val binding: LayoutCueItemBinding,
    private val cueClickListener: CueClickListener,
    private val cueTimeClickListener: CueTimeClickListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cue: Cue, isVisible: Boolean) {
        binding.apply {
            viewVisible.isVisible = isVisible
            with(cue) {
                tvText.setText(text)

                tvTime.movementMethod = LinkMovementMethod()
                tvTime.text = getTimeTextSpan(cue)

                root.setOnClickListener {
                    cueClickListener.onCueClick(absoluteAdapterPosition, this@with)
                }
            }
        }
    }

    private fun getTimeTextSpan(cue: Cue): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        builder.append(
            cue.startTime.getFormattedTime(),
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    cueTimeClickListener.onCueStartTimeClick(cue.startTime)
                }

                override fun updateDrawState(paint: TextPaint) {}
            },
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        builder.append("|")
        builder.append(
            cue.endTime.getFormattedTime(),
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    cueTimeClickListener.onCueEndTimeClick(cue.endTime)
                }

                override fun updateDrawState(paint: TextPaint) {}
            },
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        return builder
    }
}
