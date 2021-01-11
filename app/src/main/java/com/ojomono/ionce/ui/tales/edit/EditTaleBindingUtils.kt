package com.ojomono.ionce.ui.tales.edit

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ojomono.ionce.R
import com.ojomono.ionce.models.TaleModel

@BindingAdapter("taleCoverSrc")
fun ImageView.setTaleCoverSrc(tale: TaleModel) {
//    Glide.with(context)
//        .load(tale?.photoUrl)
//        .circleCrop()
//        .fallback(R.drawable.ic_profile_black_24)
//        .into(this)
}
