package com.ojomono.ionce.ui.tales

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.Tale

@BindingAdapter("title")
fun TextView.setTitle(item: Tale) {
    text = item.title
}