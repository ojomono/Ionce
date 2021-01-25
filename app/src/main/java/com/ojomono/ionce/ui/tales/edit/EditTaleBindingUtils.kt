package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.ImageUtils

@BindingAdapter("taleCoverSrc")
fun ImageView.setTaleCoverSrc(uri: Uri?) {
    val radius = context.resources.getDimensionPixelSize(R.dimen.edit_tale_cover_corners_radius)
    ImageUtils.load(context, uri, this) { roundedCorners(radius) }
}

@BindingAdapter("clearButtonEnabled")
fun Button.setClearButtonEnabled(uri: Uri?) {
    isEnabled = (uri != null)
}
