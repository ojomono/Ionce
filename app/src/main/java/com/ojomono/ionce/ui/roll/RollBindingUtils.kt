package com.ojomono.ionce.ui.roll

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ojomono.ionce.models.TaleItemModel

const val TITLE_LINES_FOR_TALE_WITH_COVER = 2

@BindingAdapter("noTalesTextVisibility")
fun TextView.setNoTalesTextVisibility(tales: List<TaleItemModel>?) {
    visibility = if (tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("rollScreenVisibility")
fun LinearLayout.setRollScreenVisibility(tales: List<TaleItemModel>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.VISIBLE else View.GONE
}

@BindingAdapter("rolledCardVisibility")
fun CardView.setRolledCardVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.GONE else View.VISIBLE
}

@BindingAdapter("titleTextLinesAndEllipsize")
fun TextView.setTitleTextLinesAndEllipsize(coverUri: String?) {
    ellipsize = if (coverUri.isNullOrEmpty()) {
        setLines(Int.MAX_VALUE)
        null
    } else {
        setLines(TITLE_LINES_FOR_TALE_WITH_COVER)
        TextUtils.TruncateAt.END
    }
}

@BindingAdapter("coverSrcAndVisibility")
fun ImageView.setCoverSrcAndVisibility(coverUri: String?) {
    val requestManager = Glide.with(context)
    visibility = if (coverUri.isNullOrEmpty()) {
        requestManager.clear(this)
        View.GONE
    } else {
        requestManager.load(Uri.parse(coverUri)).into(this)
        View.VISIBLE
    }
}
