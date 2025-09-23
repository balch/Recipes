package org.balch.recipes.xml.features.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.balch.recipes.core.models.MealDescriptor
import org.balch.recipes.databinding.FragmentSearchItemBinding

class SearchAdapter : ListAdapter<MealDescriptor, SearchAdapter.ViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(binding: FragmentSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val idView: TextView = binding.itemNumber
        private val contentView: TextView = binding.content

        fun bind(item: MealDescriptor) {
            idView.text = item.id
            contentView.text = item.name
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}

class MealDiffCallback : DiffUtil.ItemCallback<MealDescriptor>() {
    override fun areItemsTheSame(oldItem: MealDescriptor, newItem: MealDescriptor): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MealDescriptor, newItem: MealDescriptor): Boolean {
        return oldItem.id == newItem.id
    }
}