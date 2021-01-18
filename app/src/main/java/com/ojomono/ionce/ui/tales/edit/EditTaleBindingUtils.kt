package com.ojomono.ionce.ui.tales.edit

import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.Utils

@BindingAdapter("taleCoverSrc")
fun ImageView.setTaleCoverSrc(uri: Uri?) {
    if (uri == null) Glide.with(context).clear(this)
    else {
        val radius = context.resources.getDimensionPixelSize(R.dimen.edit_tale_cover_corners_radius)
        Glide.with(context).load(uri)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(Utils.getCircularProgressDrawable(context))
            .into(this)
    }
}

@BindingAdapter("clearButtonEnabled")
fun Button.setClearButtonEnabled(uri: Uri?) {
    isEnabled = (uri != null)
}
