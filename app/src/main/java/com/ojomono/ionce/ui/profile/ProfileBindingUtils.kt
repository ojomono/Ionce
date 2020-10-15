package com.ojomono.ionce.ui.profile

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.google.firebase.auth.UserInfo
import com.ojomono.ionce.R

@BindingAdapter("userPhotoSrc")
fun ImageView.setUserPhotoSrc(user: FirebaseUser?) {
    Glide.with(context)
        .load(user?.photoUrl)
        .circleCrop()
        .fallback(R.drawable.ic_profile_black_24)
        .into(this)
}

@BindingAdapter("providerData", "providerNameResId")
fun ImageView.setProviderIconSrcAndDescription(userInfo: UserInfo?, providerName: String) {
    val imageResource: Int
    val contentDescriptionResource: Int
    if (userInfo == null) {
        imageResource = R.drawable.ic_add_black_24
        contentDescriptionResource = R.string.profile_providers_add_content_description
    } else {
        imageResource = R.drawable.ic_delete_black_24
        contentDescriptionResource = R.string.profile_providers_delete_content_description
    }
    setImageResource(imageResource)
    contentDescription = context.getString(contentDescriptionResource, providerName)
}