package com.ojomono.ionce.ui.roll.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ojomono.ionce.databinding.ItemUserBinding
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseListAdapter

/**
 * [RecyclerView.Adapter] that can display a [UserItemModel] taleItem.
 */
class UsersListAdapter(title: String, listener: UsersListener, private val currentUid: String) :
    BaseListAdapter<UsersListAdapter.UserViewHolder>(title, listener) {

    /**
     * Provide a reference to the views for each model item.
     */
    class UserViewHolder(override val binding: ItemUserBinding, private val currentUid: String) :
        BaseViewHolder<UserItemModel, UsersListener>(binding) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        override fun bind(item: UserItemModel, clickListener: UsersListener?) {
            binding.currentUid = currentUid
            binding.userItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun buildViewHolderFrom(parent: ViewGroup): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding, currentUid)
    }

    /**
     * The interface for listening to taleItem events.
     */
    interface UsersListener : BaseListener {
        fun onTales(userItem: UserItemModel)   // Tales icon clicked
    }

}