package com.ojomono.ionce.ui.tales

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ojomono.ionce.databinding.ItemTaleBinding
import com.ojomono.ionce.models.Tale


/**
 * [RecyclerView.Adapter] that can display a [Tale] item.
 */
class TalesAdapter(private val clickListener: TalesListener) :
    ListAdapter<Tale, TalesAdapter.ViewHolder>(TalesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    /**
     * [RecyclerView.ViewHolder] that describes a [Tale] item view and metadata about its place within
     * the RecyclerView.
     */
    class ViewHolder private constructor(private val binding: ItemTaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        fun bind(item: Tale, clickListener: TalesListener) {
            binding.tale = item
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
     * [DiffUtil.ItemCallback] used to determine the diff between two lists of tales in order to
     * optimize the [RecyclerView] for changes to the data.
     */
    class TalesDiffCallback : DiffUtil.ItemCallback<Tale>() {
        override fun areItemsTheSame(oldItem: Tale, newItem: Tale): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tale, newItem: Tale): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * The interface for listening to item events.
     */
    interface TalesListener {
        fun onEditTaleClicked(tale: Tale)   // Edit icon clicked
        fun onDeleteTaleClicked(tale: Tale) // Delete icon clicked
    }

}