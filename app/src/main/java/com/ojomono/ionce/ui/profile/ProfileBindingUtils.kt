package com.ojomono.ionce.ui.profile

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FacebookAuthProvider    // TODO avoid importing firebase packages here
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.google.firebase.auth.TwitterAuthProvider    // TODO avoid importing firebase packages here
import com.ojomono.ionce.R

@BindingAdapter("userPhotoSrc")
fun ImageView.setUserPhotoSrc(user: FirebaseUser?) {
    Glide.with(context)
        .load(user?.photoUrl)
        .circleCrop()
        .fallback(R.drawable.ic_profile_grey_72)
        .into(this)
}

@BindingAdapter("twitterLinkText")
fun TextView.setTwitterLinkText(user: FirebaseUser?) {
    text =
        user?.providerData?.find { it.providerId == TwitterAuthProvider.PROVIDER_ID }?.displayName
}

@BindingAdapter("facebookLinkText")
fun TextView.setFacebookLinkText(user: FirebaseUser?) {
    text =
        user?.providerData?.find { it.providerId == FacebookAuthProvider.PROVIDER_ID }?.displayName
}