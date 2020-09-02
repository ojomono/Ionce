package com.ojomono.ionce.ui.roll

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.models.TaleItemData

@BindingAdapter("hintText")
fun TextView.setHintText(tales: List<TaleItemData>?) {
    text = if (tales.isNullOrEmpty()) resources.getString(R.string.roll_hint_no_tales_text)
    else resources.getString(R.string.roll_hint_default_text)
}

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(taleText: String?) {
    visibility = if (taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("taleCardVisibility")
fun CardView.setTaleCardVisibility(taleText: String?) {
    visibility = if (!taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}