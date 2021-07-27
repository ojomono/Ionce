package com.ojomono.ionce.ui.roll.group

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ojomono.ionce.databinding.ItemUserBinding
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.ui.bases.BaseListAdapter

class UsersListAdapter(title: String) :
    BaseListAdapter<UsersListAdapter.UserViewHolder>(title, null) {

    /**
     * Provide a reference to the views for each model item.
     */
    class UserViewHolder(override val binding: ItemUserBinding) :
        BaseViewHolder<UserItemModel, BaseListener>(binding) {

        override fun toString(): String {
            return super.toString() + " '" + binding.textTitle.text + "'"
        }

        override fun bind(item: UserItemModel, clickListener: BaseListener?) {
            binding.userItem = item
            binding.executePendingBindings()
        }
    }

    override fun buildViewHolderFrom(parent: ViewGroup): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }
}