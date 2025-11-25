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

package com.teixeira0x.subtypo.ui.optionlist.adapter

import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutOptionItemBinding
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem

class OptionViewHolder(
    private val binding: LayoutOptionItemBinding,
    private val optionClickListener: (Int, OptionItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, option: OptionItem) {
        binding.apply {
            root.setOnClickListener { optionClickListener(position, option) }
            imgIcon.setImageResource(option.icon)
            tvTitle.text = option.title
        }
    }
}
