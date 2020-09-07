package com.ojomono.ionce.ui.profile

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser    // TODO avoid importing firebase packages here
import com.google.firebase.auth.TwitterAuthProvider

@BindingAdapter("twitterText")
fun TextView.setTwitterText(user: FirebaseUser?) {
    text =
        user?.providerData?.find { it.providerId == TwitterAuthProvider.PROVIDER_ID }?.displayName
}

@BindingAdapter("facebookText")
fun TextView.setFacebookText(user: FirebaseUser?) {
    text =
        user?.providerData?.find { it.providerId == FacebookAuthProvider.PROVIDER_ID }?.displayName
}