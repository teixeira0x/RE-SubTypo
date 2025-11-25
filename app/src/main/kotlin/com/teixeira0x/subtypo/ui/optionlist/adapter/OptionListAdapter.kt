package com.teixeira0x.subtypo.ui.optionlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.core.ui.util.layoutInflater
import com.teixeira0x.subtypo.databinding.LayoutOptionItemBinding
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem

class OptionListAdapter(
    private val options: List<OptionItem>,
    private val optionClickListener: (Int, OptionItem) -> Unit,
) : RecyclerView.Adapter<OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        return OptionViewHolder(
            LayoutOptionItemBinding.inflate(parent.context.layoutInflater, parent, false),
            optionClickListener,
        )
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(position, options[position])
    }

    override fun getItemCount() = options.size
}
