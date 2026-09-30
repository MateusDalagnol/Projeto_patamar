package com.patamar.app.ui.explore.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.patamar.app.data.model.Event
import com.patamar.app.databinding.ItemEventCompactBinding

class EventListAdapter(
    private val onClick: (Event) -> Unit
) : ListAdapter<Event, EventListAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemEventCompactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.apply {
            tvName.text = event.name
            tvCategory.text = event.category.label
            tvDate.text = "${event.date}"
            tvDistance.text = formatDistance(event.distanceMeters)
            val categoryColor = try {
                Color.parseColor(event.category.colorHex)
            } catch (e: IllegalArgumentException) {
                Color.parseColor("#3F3F46")
            }
            ivThumbnail.load(event.imageUrl) {
                crossfade(true)
                placeholder(ColorDrawable(categoryColor))
                error(ColorDrawable(categoryColor))
                transformations(CircleCropTransformation())
            }
            rowContent.setOnClickListener { onClick(event) }
        }
    }

    private fun formatDistance(meters: Int): String =
        if (meters >= 1000) "%.1f km".format(meters / 1000.0) else "$meters m"

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
        }
    }
}
