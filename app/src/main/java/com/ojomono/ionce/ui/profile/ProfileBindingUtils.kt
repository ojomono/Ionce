package com.ojomono.ionce.ui.profile

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.UserInfo        // TODO avoid importing firebase packages here
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.ImageUtils.loadUriToImageView

@BindingAdapter("userPhotoSrc")
fun ImageView.setUserPhotoSrc(uri: Uri?) {
    loadUriToImageView(context, uri, this) {
        error(R.drawable.ic_profile_photo_broken_image)
        fallback(R.drawable.ic_profile_photo_person)
        circleCrop()
    }
}

@BindingAdapter("providerData", "providerNameResId")
fun ImageView.setProviderIconSrcAndDescription(providerData: UserInfo?, providerName: String) {
    val imageResource: Int
    val contentDescriptionResource: Int
    if (providerData == null) {
        imageResource = R.drawable.ic_baseline_add_24
        contentDescriptionResource = R.string.profile_providers_add_content_description
    } else {
        imageResource = R.drawable.ic_baseline_delete_24
        contentDescriptionResource = R.string.profile_providers_delete_content_description
    }
    setImageResource(imageResource)
    contentDescription = context.getString(contentDescriptionResource, providerName)
}
