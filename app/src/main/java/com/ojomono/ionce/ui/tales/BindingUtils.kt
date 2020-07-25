package com.ojomono.ionce.ui.tales

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("title")
fun TextView.setTitle(item: Tale) {
    text = item.title
}