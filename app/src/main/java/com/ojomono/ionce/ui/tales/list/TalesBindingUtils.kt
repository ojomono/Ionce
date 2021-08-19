package com.ojomono.ionce.ui.tales.list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ojomono.ionce.R
import com.ojomono.ionce.models.TaleItemModel

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(tales: List<TaleItemModel>?) {
    visibility = if (tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("hintText")
fun TextView.setHintText(shownList: TalesViewModel.ListType?) {
    text = resources.getString(
        when (shownList) {
            TalesViewModel.ListType.MY_TALES -> R.string.tales_hint_text_my
            TalesViewModel.ListType.HEARD_TALES -> R.string.tales_hint_text_heard
            else -> R.string.tales_hint_text_my // Default to my tales
        }
    )
}

@BindingAdapter("talesListVisibility")
fun RecyclerView.setTalesListVisibility(tales: List<TaleItemModel>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("addTaleFabVisibility")
fun FloatingActionButton.setAddTaleFabVisibility(shownList: TalesViewModel.ListType?) {
    visibility = if (shownList == TalesViewModel.ListType.MY_TALES) View.VISIBLE else View.GONE
}

@BindingAdapter("itemIconsVisibility")
fun ImageView.setItemIconsVisibility(shownList: TalesViewModel.ListType?) {
    visibility = if (shownList == TalesViewModel.ListType.MY_TALES) View.VISIBLE else View.GONE
}

@BindingAdapter("itemOwnerVisibility")
fun TextView.setItemOwnerVisibility(shownList: TalesViewModel.ListType?) {
    visibility = if (shownList == TalesViewModel.ListType.MY_TALES) View.GONE else View.VISIBLE
}