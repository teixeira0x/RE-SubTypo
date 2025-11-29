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

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.ui.util.layoutInflater
import com.teixeira0x.subtypo.databinding.LayoutCueItemBinding

class CueListAdapter(
    private val cueClickListener: CueClickListener,
    private val cueTimeClickListener: CueTimeClickListener,
) : RecyclerView.Adapter<CueListViewHolder>() {

    private var recyclerView: RecyclerView? = null

    private var visibleCueIndices: List<Int> = emptyList()

    var cues: List<Cue> = emptyList()
        private set

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CueListViewHolder {
        return CueListViewHolder(
            LayoutCueItemBinding.inflate(parent.context.layoutInflater, parent, false),
            cueClickListener,
            cueTimeClickListener,
        )
    }

    override fun onBindViewHolder(holder: CueListViewHolder, position: Int) {
        val isVisible = visibleCueIndices.contains(position)
        holder.bind(
            cues.getOrNull(position - 1),
            cues[position],
            cues.getOrNull(position + 1),
            isVisible
        )
    }

    override fun getItemCount() = cues.size

    fun submitList(cues: List<Cue>) {
        this.cues = cues
        notifyDataSetChanged()
    }

    fun updateVisibleCues(playerPosition: Long) {
        val newVisibleCueIndices =
            cues.mapIndexedNotNull { index, cue ->
                if (playerPosition in cue.startTime..cue.endTime) index else null
            }

        if (newVisibleCueIndices != visibleCueIndices) {
            val oldVisibleIndices = visibleCueIndices
            visibleCueIndices = newVisibleCueIndices

            oldVisibleIndices.forEach { index -> notifyItemChanged(index) }
            newVisibleCueIndices.forEach { index -> notifyItemChanged(index) }

            newVisibleCueIndices.lastOrNull()?.let { recyclerView?.scrollToPosition(it) }
        }
    }
}
