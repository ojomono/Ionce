package com.ojomono.ionce.ui.profile

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.google.firebase.auth.UserInfo        // TODO avoid importing firebase packages here
import com.ojomono.ionce.R

@BindingAdapter("userPhotoSrc")
fun ImageView.setUserPhotoSrc(uri: Uri?) {
    Glide.with(context)
        .load(uri)
        .circleCrop()
        .fallback(R.drawable.ic_profile_black_24)
        .into(this)
}

@BindingAdapter("providerData", "providerNameResId")
fun ImageView.setProviderIconSrcAndDescription(providerData: UserInfo?, providerName: String) {
    val imageResource: Int
    val contentDescriptionResource: Int
    if (providerData == null) {
        imageResource = R.drawable.ic_add_black_24
        contentDescriptionResource = R.string.profile_providers_add_content_description
    } else {
        imageResource = R.drawable.ic_delete_black_24
        contentDescriptionResource = R.string.profile_providers_delete_content_description
    }
    setImageResource(imageResource)
    contentDescription = context.getString(contentDescriptionResource, providerName)
}
