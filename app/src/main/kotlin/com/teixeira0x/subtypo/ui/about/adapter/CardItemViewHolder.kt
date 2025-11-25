package com.teixeira0x.subtypo.ui.about.adapter

import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.databinding.LayoutAboutCardItemBinding
import com.teixeira0x.subtypo.ui.about.model.CardItem

class CardItemViewHolder(private val binding: LayoutAboutCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CardItem) {
        binding.apply {
            root.setOnClickListener { item.action() }
            imgIcon.setImageResource(item.icon)
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
        }
    }
}
