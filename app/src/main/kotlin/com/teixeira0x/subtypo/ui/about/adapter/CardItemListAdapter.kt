package com.teixeira0x.subtypo.ui.about.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.core.ui.util.layoutInflater
import com.teixeira0x.subtypo.databinding.LayoutAboutCardItemBinding
import com.teixeira0x.subtypo.ui.about.model.CardItem

class CardItemListAdapter(private val cardItems: List<CardItem>) :
    RecyclerView.Adapter<CardItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        return CardItemViewHolder(
            LayoutAboutCardItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        holder.bind(cardItems[position])
    }

    override fun getItemCount() = cardItems.size
}
