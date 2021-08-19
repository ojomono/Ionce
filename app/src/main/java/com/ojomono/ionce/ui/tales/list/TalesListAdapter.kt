package com.ojomono.ionce.ui.tales.list

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.ojomono.ionce.databinding.ItemTaleBinding
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.ui.bases.BaseListAdapter
import java.lang.IllegalStateException

/**
 * [RecyclerView.Adapter] that can display a [TaleItemModel] taleItem.
 */
class TalesListAdapter(
    title: String,
    listener: TalesListener,
    private val shownListLiveData: LiveData<Int>,
) : BaseListAdapter<TalesListAdapter.TaleViewHolder>(title, listener) {

    /**
     * Provide a reference to the views for each model item.
     */
    class TaleViewHolder(override val binding: ItemTaleBinding, private val shownList: Int) :
        BaseViewHolder<TaleItemModel, TalesListener>(binding) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        override fun bind(item: TaleItemModel, clickListener: TalesListener?) {
            binding.shownList = shownList
            binding.taleItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun buildViewHolderFrom(parent: ViewGroup): TaleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTaleBinding.inflate(layoutInflater, parent, false)
        return shownListLiveData.value?.let { TaleViewHolder(binding, it) }
            ?: throw IllegalStateException()
    }

    /**
     * The interface for listening to taleItem events.
     */
    interface TalesListener : BaseListener {
        fun onEdit(taleItem: TaleItemModel)     // Edit icon clicked
        fun onDelete(taleItem: TaleItemModel)   // Delete icon clicked
        // For drag n' drop feature
//        fun onMoved(fromPosition: Int, toPosition: Int)  // Row dragged and dropped
    }

}