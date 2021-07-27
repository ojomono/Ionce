package com.ojomono.ionce.ui.bases

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ojomono.ionce.databinding.ItemHeaderBinding
import com.ojomono.ionce.models.BaseItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseListAdapter<T : BaseListAdapter.BaseViewHolder<*, *>>(
    private val title: String?,
    private val listener: BaseListener?
) : ListAdapter<BaseListAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    interface BaseListener

    /**
     * Provide a reference to the views for each model item.
     */
    abstract class BaseViewHolder<modelType : BaseItemModel, listenerType : BaseListener>
    protected constructor(protected open val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: modelType, clickListener: listenerType? = null)
    }

    abstract fun buildViewHolderFrom(parent: ViewGroup): T

    /**
     * Create new views (invoked by the layout manager).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_DATA -> buildViewHolderFrom(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Used instead of the standard "submitList" in order to add the header item to the [list].
     */
    fun addHeaderAndSubmitList(list: List<BaseItemModel>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(Item.HeaderItem)
                else -> listOf(Item.HeaderItem) + list.map { Item.DataItem(it) }
            }
            withContext(Dispatchers.Main) { submitList(items) }
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     */
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BaseViewHolder<*, *> -> {
                val item = getItem(position) as Item.DataItem
                (holder as BaseViewHolder<BaseItemModel, BaseListener>).bind(item.model, listener)
            }
            is TextViewHolder -> title?.let { holder.bind(it) }
        }
    }

    /**
     * Get the right type of the view in the given [position] (invoked by the layout manager).
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is Item.DataItem -> ITEM_VIEW_TYPE_DATA
            else -> super.getItemViewType(position)
        }
    }

    /**
     * Provide a reference to the views for each header item.
     */
    class TextViewHolder(val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.title = title
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemHeaderBinding.inflate(layoutInflater, parent, false)
                return TextViewHolder(binding)
            }
        }
    }

    /**
     * The [DiffUtil.ItemCallback] used to determine the diff between two lists in order to optimize
     * the [RecyclerView] for changes to the model.
     */
    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Types of possible items in the RecyclerView.
     */
    sealed class Item {
        abstract val id: String

        data class DataItem(val model: BaseItemModel) : Item() {
            override val id = model.id
        }

        object HeaderItem : Item() {
            override val id = ""
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_DATA = 1
    }
}