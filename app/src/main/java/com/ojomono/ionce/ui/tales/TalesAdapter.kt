package com.ojomono.ionce.ui.tales

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.ItemTaleBinding
import com.ojomono.ionce.models.TaleItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [RecyclerView.Adapter] that can display a [TaleItemModel] taleItem.
 */
class TalesAdapter(private val listener: TalesListener) :
    ListAdapter<TalesAdapter.DataItem, RecyclerView.ViewHolder>(TalesDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    /**
     * Create new views (invoked by the layout manager).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Used instead of the standard "submitList" in order to add the header item to the [list].
     */
    fun addHeaderAndSubmitList(list: List<TaleItemModel>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.TaleItem(it) }
            }
            withContext(Dispatchers.Main) { submitList(items) }
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val talesItem = getItem(position) as DataItem.TaleItem
                holder.bind(talesItem.model, listener)
            }
        }
    }

    /**
     * Get the right type of the view in the given [position] (invoked by the layout manager).
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.TaleItem -> ITEM_VIEW_TYPE_ITEM
            else -> super.getItemViewType(position)
        }
    }

    // For drag n' drop feature
//    /**
//     * Modify the tales list that an item is moved from [fromPosition] to [toPosition].
//     */
//    fun onRowMoved(fromPosition: Int, toPosition: Int) {
//        // Only the adapter knows about the header - so the positions in the actual list are -1
//        if (toPosition > 0) listener.onMoved(fromPosition - 1, toPosition - 1) else null
//    }

    /**
     * Provide a reference to the views for each header item.
     */
    class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    /**
     * Provide a reference to the views for each model item.
     */
    class ViewHolder private constructor(private val binding: ItemTaleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        fun bind(taleItem: TaleItemModel, clickListener: TalesListener) {
            binding.taleItem = taleItem
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
     * The [DiffUtil.ItemCallback] used to determine the diff between two lists of taleItem in order
     * to optimize the [RecyclerView] for changes to the model.
     */
    class TalesDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * The interface for listening to taleItem events.
     */
    interface TalesListener {
        fun onEdit(taleItem: TaleItemModel)     // Edit icon clicked
        fun onDelete(taleItem: TaleItemModel)   // Delete icon clicked
        // For drag n' drop feature
//        fun onMoved(fromPosition: Int, toPosition: Int)  // Row dragged and dropped
    }

    /**
     * Types of possible items in the RecyclerView.
     */
    sealed class DataItem {
        abstract val id: String

        data class TaleItem(val model: TaleItemModel) : DataItem() {
            override val id = model.id
        }

        object Header : DataItem() {
            override val id = ""
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_ITEM = 1
    }
}