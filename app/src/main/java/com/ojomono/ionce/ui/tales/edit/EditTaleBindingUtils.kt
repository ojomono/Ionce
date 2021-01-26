package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.ImageUtils
import com.ojomono.ionce.utils.ImageUtils.UPLOADING_IN_PROGRESS

@BindingAdapter("taleCoverSrc")
fun ImageView.setTaleCoverSrc(uri: Uri?) {
    val radius = context.resources.getDimensionPixelSize(R.dimen.edit_tale_cover_corners_radius)
    ImageUtils.load(context, uri, this) { roundedCorners(radius) }
}

@BindingAdapter("taleCoverClickable")
fun FrameLayout.setTaleCoverClickable(uri: Uri?) {
    isClickable = (uri != Uri.parse(UPLOADING_IN_PROGRESS))
}

@BindingAdapter("coverIconVisibility")
fun ImageView.setCoverIconVisibility(uri: Uri?) {
    visibility = if (uri == Uri.parse(UPLOADING_IN_PROGRESS)) View.GONE else View.VISIBLE
}

@BindingAdapter("clearButtonEnabled")
fun Button.setClearButtonEnabled(uri: Uri?) {
    isEnabled = ((uri != null) and (uri != Uri.parse(UPLOADING_IN_PROGRESS)))
}
