package com.ojomono.ionce.ui.tales

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ojomono.ionce.models.TaleItemData

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(tales: List<TaleItemData>?) {
    visibility = if (tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("talesListVisibility")
fun RecyclerView.setTalesListVisibility(tales: List<TaleItemData>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}