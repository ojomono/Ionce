package com.ojomono.ionce.ui.profile

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.ojomono.ionce.R

@BindingAdapter("userPhotoSrc")
fun ImageView.setUserPhotoSrc(user: FirebaseUser?) {
    Glide.with(context)
        .load(user?.photoUrl)
        .circleCrop()
        .fallback(R.drawable.ic_profile_black_24)
        .into(this)
}
