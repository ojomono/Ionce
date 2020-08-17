package com.ojomono.ionce.ui.tales

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ojomono.ionce.databinding.ItemTaleBinding
import com.ojomono.ionce.models.TalesItem


/**
 * [RecyclerView.Adapter] that can display a [TalesItem] item.
 */
class TalesAdapter(private val clickListener: TalesListener) :
    ListAdapter<TalesItem, TalesAdapter.ViewHolder>(TalesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    /**
     * [RecyclerView.ViewHolder] that describes a [TalesItem] item view and metadata about its place within
     * the RecyclerView.
     */
    class ViewHolder private constructor(private val binding: ItemTaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        fun bind(item: TalesItem, clickListener: TalesListener) {
            binding.talesItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ItemTaleBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    /**
     * [DiffUtil.ItemCallback] used to determine the diff between two lists of talesItems in order to
     * optimize the [RecyclerView] for changes to the data.
     */
    class TalesDiffCallback : DiffUtil.ItemCallback<TalesItem>() {
        override fun areItemsTheSame(oldItem: TalesItem, newItem: TalesItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TalesItem, newItem: TalesItem): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * The interface for listening to item events.
     */
    interface TalesListener {
        fun onEdit(item: TalesItem)   // Edit icon clicked
        fun onDelete(item: TalesItem) // Delete icon clicked
    }

}