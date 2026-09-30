package com.patamar.app.ui.explore.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.patamar.app.R
import com.patamar.app.data.model.Event
import com.patamar.app.databinding.ItemEventFeaturedBinding

class FeaturedEventAdapter(
    private val onClick: (Event) -> Unit,
    private val onSaveClick: (Event) -> Unit
) : ListAdapter<Event, FeaturedEventAdapter.ViewHolder>(DIFF) {

    private var savedIds: Set<String> = emptySet()

    inner class ViewHolder(val binding: ItemEventFeaturedBinding) : RecyclerView.ViewHolder(binding.root)

    // Atualiza só a estrela dos cards (payload), sem recarregar foto nem texto.
    fun updateSaved(ids: Set<String>) {
        if (ids == savedIds) return
        savedIds = ids
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SAVED)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventFeaturedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_SAVED)) {
            bindStar(holder, getItem(position), animate = true)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.apply {
            tvName.text = event.name
            tvDate.text = "${event.date} · ${event.time}"
            tvCategoryBadge.text = event.category.label

            // BETA: enquanto a foto carrega (ou se falhar), mostra a cor da categoria
            // em vez de deixar o card em branco.
            val categoryColor = try {
                Color.parseColor(event.category.colorHex)
            } catch (e: IllegalArgumentException) {
                Color.parseColor("#3F3F46")
            }
            ivCover.load(event.imageUrl) {
                crossfade(true)
                placeholder(ColorDrawable(categoryColor))
                error(ColorDrawable(categoryColor))
            }

            root.setOnClickListener { onClick(event) }
            btnSave.setOnClickListener { onSaveClick(event) }
        }
        bindStar(holder, event, animate = false)
    }

    private fun bindStar(holder: ViewHolder, event: Event, animate: Boolean) {
        val button = holder.binding.btnSave
        val context = button.context
        val saved = event.id in savedIds
        button.setImageResource(if (saved) R.drawable.ic_star else R.drawable.ic_star_outline)
        button.imageTintList = ColorStateList.valueOf(
            if (saved) ContextCompat.getColor(context, R.color.star_saved) else Color.WHITE
        )
        button.contentDescription = context.getString(if (saved) R.string.event_unsave else R.string.event_save)
        if (animate && saved) {
            button.animate().cancel()
            button.scaleX = 0.7f
            button.scaleY = 0.7f
            button.animate().scaleX(1f).scaleY(1f).setDuration(180).start()
        }
    }

    companion object {
        private const val PAYLOAD_SAVED = "saved"

        val DIFF = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
        }
    }
}
