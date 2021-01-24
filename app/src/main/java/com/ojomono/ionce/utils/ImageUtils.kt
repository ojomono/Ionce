package com.ojomono.ionce.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.color.MaterialColors
import com.ojomono.ionce.R
import java.io.*


/**
 * Handles all interactions with image-related extensions.
 */
object ImageUtils {

    /***************/
    /** constants **/
    /***************/

    // Compression constants
    private const val FILE_ACCESS_MODE_READ = "r"
    private const val COMPRESS_QUALITY = 80

    // Used compression format for images - Used also in Storage to determine file extension!
    // If changed, user photo will be uploaded with different name! May not override old one!
    val COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG

    /************/
    /** fields **/
    /************/

    // Available loading customization fields
    private var errorResId: Int = R.drawable.ic_baseline_broken_image_24
    private var fallbackResId: Int = R.color.fui_transparent
    private var transformation: Transformation<Bitmap>? = null

    // Available loading customization
    object Loader {
        fun default() {
            errorResId = R.drawable.ic_baseline_broken_image_24
            fallbackResId = R.color.fui_transparent
            transformation = null
        }

        fun error(resourceId: Int) = run { errorResId = resourceId }
        fun fallback(resourceId: Int) = run { fallbackResId = resourceId }
        fun circleCrop() = run { transformation = CircleCrop() }
        fun roundedCorners(radius: Int) = run { transformation = RoundedCorners(radius) }
    }

    /********************/
    /** public methods **/
    /********************/

    /**
     * Load the given [uri] to the given [view]. Custom loading can be achieved using [loadPatch].
     */
    fun loadUriToImageView(
        context: Context,
        uri: Uri?,
        view: ImageView,
        loadPatch: Loader.() -> Unit = { default() }
    ) {
        Loader.apply(loadPatch)
        val transformations: MutableList<Transformation<Bitmap>> = mutableListOf(CenterCrop())
        transformation?.let { transformations.add(it) }

        Glide.with(context)
            .load(uri)
            .transform(*transformations.toTypedArray())
            .fallback(fallbackResId)
            .error(errorResId)
            .placeholder(getCircularProgressDrawable(view))
            .into(view)
    }

    /**
     * Compress the given [uri] and return the result bitmap.
     */
    fun uriToCompressedBitmap(context: Context, uri: Uri): ByteArray {
        // TODO: Get this operation off the UI thread

        // Get file bitmap
        val pfd = context.contentResolver.openFileDescriptor(uri, FILE_ACCESS_MODE_READ)
        val bitmap =
            BitmapFactory.decodeFileDescriptor(pfd?.fileDescriptor, null, null)

        // Get compressed bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, baos)
        return baos.toByteArray()
    }

//    fun compressAndSetImage(activity: Activity, uri: Uri) {
//        val job = Job()
//        val uiScope = CoroutineScope(Dispatchers.IO + job)
//        val fileUri = getFilePathFromUri(activity, uri)
//        uiScope.launch {
//            val compressedImageFile = activity?.let {
//                fileUri?.path?.let { path ->
//                    Compressor.compress(it, File(path)) {
//                        quality(75) // combine with compressor constraint
//                        format(Bitmap.CompressFormat.JPEG)
//                    }
//                }
//            }
//            val resultUri = Uri.fromFile(compressedImageFile)
//            activity.runOnUiThread { resultUri?.let { viewModel.updateUserPicture(resultUri) } }
//        }
//    }
//
//    private fun getFilePathFromUri(context: Context?, uri: Uri?): Uri? {
//        val fileName: String = getFileName(context, uri)
//        val file = File(context?.externalCacheDir, fileName)
//        file.createNewFile()
//        FileOutputStream(file).use { outputStream ->
//            if (uri != null) {
//                context?.contentResolver?.openInputStream(uri).use { inputStream ->
//                    copyFile(inputStream, outputStream)
//                    outputStream.flush()
//                }
//            }
//        }
//        return Uri.fromFile(file)
//    }
//
//    private fun getFileName(context: Context?, uri: Uri?): String {
//        var fileName: String? = getFileNameFromCursor(uri, context)
//        if (fileName == null) {
//            val fileExtension: String? = getFileExtension(uri, context)
//            fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
//        } else if (!fileName.contains(".")) {
//            val fileExtension: String? = getFileExtension(uri, context)
//            fileName = "$fileName.$fileExtension"
//        }
//        return fileName
//    }
//
//    private fun getFileNameFromCursor(uri: Uri?, context: Context?): String? {
//        val fileCursor: Cursor? = uri?.let {
//            context?.contentResolver
//                ?.query(
//                    it, arrayOf<String>(OpenableColumns.DISPLAY_NAME),
//                    null, null, null
//                )
//        }
//        var fileName: String? = null
//        if (fileCursor != null && fileCursor.moveToFirst()) {
//            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            if (cIndex != -1) {
//                fileName = fileCursor.getString(cIndex)
//            }
//        }
//        fileCursor?.close()
//        return fileName
//    }
//
//    private fun getFileExtension(uri: Uri?, context: Context?): String? {
//        val fileType: String? = uri?.let { context?.contentResolver?.getType(it) }
//        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
//    }
//
//    private fun copyFile(input: InputStream?, output: OutputStream) {
//        val buffer = ByteArray(1024)
//        var read: Int? = null
//        while (input?.read(buffer).also { read = it } != -1) {
//            read?.let { output.write(buffer, 0, it) }
//        }
//    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Get a default CircularProgressDrawable for [view].
     */
    private fun getCircularProgressDrawable(view: ImageView) =
        androidx.swiperefreshlayout.widget.CircularProgressDrawable(view.context).apply {
            setStyle(androidx.swiperefreshlayout.widget.CircularProgressDrawable.LARGE)
            setColorSchemeColors(MaterialColors.getColor(view, R.attr.colorSecondary))
            start()
        }
}