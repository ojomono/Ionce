package com.ojomono.ionce.ui.roll

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.TaleItemModel

@BindingAdapter("noTalesTextVisibility")
fun TextView.setNoTalesTextVisibility(tales: List<TaleItemModel>?) {
    visibility = if (tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("rollScreenVisibility")
fun LinearLayout.setRollScreenVisibility(tales: List<TaleItemModel>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(taleText: String?) {
    visibility = if (taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("taleCardVisibility")
fun CardView.setTaleCardVisibility(taleText: String?) {
    visibility = if (!taleText.isNullOrEmpty()) View.VISIBLE else View.GONE
}