package com.ojomono.ionce.utils.proxies

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.color.MaterialColors
import com.ojomono.ionce.R


/**
 * Handles all image loading - wrapping Glide.
 */
object ImageLoader {

    // Available loading customization fields
    private var errorResId: Int = R.drawable.ic_baseline_broken_image_24
    private var fallbackResId: Int = com.firebase.ui.auth.R.color.fui_transparent
    private var transformation: Transformation<Bitmap>? = null

    // Available loading customization
    object Loader {
        fun default() {
            errorResId = R.drawable.ic_baseline_broken_image_24
            fallbackResId = com.firebase.ui.auth.R.color.fui_transparent
            transformation = null
        }

        fun error(resourceId: Int) = run { errorResId = resourceId }
        fun fallback(resourceId: Int) = run { fallbackResId = resourceId }
        fun circleCrop() = run { transformation = CircleCrop() }
        fun roundedCorners(radius: Int) = run { transformation = RoundedCorners(radius) }
    }

    /**
     * Load the given [uri] to the given [view]. Custom loading can be achieved using [loadPatch].
     */
    fun load(
        context: Context?,
        uri: Uri?,
        view: ImageView,
        loadPatch: Loader.() -> Unit = { default() }
    ) {
        Loader.apply(loadPatch)

        val transformations: MutableList<Transformation<Bitmap>> = mutableListOf(CenterCrop())
        transformation?.let { transformations.add(it) }

        if (context != null) {
            Glide.with(context)
                .load(uri)
                .transform(*transformations.toTypedArray())
                .placeholder(getCircularProgressDrawable(view))
                .fallback(fallbackResId)
                .error(errorResId)
                .into(view)
        }
    }

//    fun load(
//        context: Context?, uriString: String?, view: ImageView,
//        loadPatch: Loader.() -> Unit = { default() }
//    ) = load(context, Uri.parse(uriString), view, loadPatch)

    private fun getCircularProgressDrawable(view: ImageView) =
        androidx.swiperefreshlayout.widget.CircularProgressDrawable(view.context).apply {
            setStyle(androidx.swiperefreshlayout.widget.CircularProgressDrawable.LARGE)
            setColorSchemeColors(
                MaterialColors.getColor(
                    view,
                    com.firebase.ui.auth.R.attr.colorSecondary
                )
            )
            start()
        }

}