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
fun TextView.setHintText(shownList: Int) {
    text = resources.getString(
        when (shownList) {
            R.id.button_my_tales -> R.string.tales_hint_text_my
            R.id.button_heard_tales -> R.string.tales_hint_text_heard
            else -> R.string.tales_hint_text_my
        }
    )
}

@BindingAdapter("talesListVisibility")
fun RecyclerView.setTalesListVisibility(tales: List<TaleItemModel>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("addTaleFabVisibility")
fun FloatingActionButton.setAddTaleFabVisibility(shownList: Int) {
    visibility = if (shownList == R.id.button_my_tales) View.VISIBLE else View.GONE
}

@BindingAdapter("itemIconsVisibility")
fun ImageView.setItemIconsVisibility(shownList: Int) {
    visibility = if (shownList == R.id.button_my_tales) View.VISIBLE else View.GONE
}