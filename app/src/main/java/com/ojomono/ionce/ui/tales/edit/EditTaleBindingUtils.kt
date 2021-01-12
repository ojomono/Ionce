package com.ojomono.ionce.ui.tales.edit

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ojomono.ionce.models.TaleModel

@BindingAdapter("taleCoverSrc")
fun ImageView.setTaleCoverSrc(tale: TaleModel?) {
    tale?.let { if (it.media.isNotEmpty()) Glide.with(context).load(it.media[0]).into(this) }
}
