package org.balch.recipes.xml.features.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import org.balch.recipes.core.models.MealDescriptor
import org.balch.recipes.databinding.ItemSearchBinding

class SearchAdapter(
    private val onItemClicked: (MealDescriptor) -> Unit
) : ListAdapter<MealDescriptor, SearchAdapter.ViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MealDescriptor) {
            binding.root.setOnClickListener {
                onItemClicked(item)
            }
            binding.recipeImage.load(item.thumbnail)
            binding.content.text = item.name
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