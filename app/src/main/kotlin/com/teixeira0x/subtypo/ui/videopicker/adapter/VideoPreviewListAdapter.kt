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

package com.teixeira0x.subtypo.ui.videopicker.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.teixeira0x.subtypo.core.media.model.Video
import com.teixeira0x.subtypo.core.ui.util.layoutInflater
import com.teixeira0x.subtypo.databinding.LayoutVideoPreviewItemBinding

class VideoPreviewListAdapter(private val onVideoPreviewClick: (Video) -> Unit) :
    ListAdapter<Video, VideoPreviewViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoPreviewViewHolder {
        return VideoPreviewViewHolder(
            LayoutVideoPreviewItemBinding.inflate(parent.context.layoutInflater, parent, false),
            onVideoPreviewClick,
        )
    }

    override fun onBindViewHolder(holder: VideoPreviewViewHolder, position: Int) {
        val video = getItem(position)
        holder.bind(video)
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
}
