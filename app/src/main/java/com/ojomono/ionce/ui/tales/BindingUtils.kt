package com.ojomono.ionce.ui.tales

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.TalesItem

@BindingAdapter("title")
fun TextView.setTitle(item: TalesItem) {
    text = item.title
}