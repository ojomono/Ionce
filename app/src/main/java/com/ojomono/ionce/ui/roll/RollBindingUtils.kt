package com.ojomono.ionce.ui.roll

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.models.TalesItem

@BindingAdapter("hintText")
fun TextView.setHintText(tales: List<TalesItem>?) {
    text = if (tales.isNullOrEmpty()) resources.getString(R.string.roll_hint_no_tales_text)
    else resources.getString(R.string.roll_hint_default_text)
}

@BindingAdapter("hintVisibility")
fun TextView.setHintVisibility(taleText: String?) {
    visibility = if (taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("taleVisibility")
fun CardView.setTaleVisibility(taleText: String?) {
    visibility = if (!taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}