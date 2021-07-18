package com.ojomono.ionce.utils.images

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Handles all image compressing - wrapping Compressor.
 */
object ImageCompressor {

    // Compression constants
    private const val COMPRESS_QUALITY = 80

    // Used compression format for images - Used also in Storage to determine file extension!
    // If changed, user photo will be uploaded with different name! May not override old one!
    val COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG

    /**
     * Compress the given image [uri], and use it as parameter for given [followup] function.
     */
    fun compress(context: Context?, uri: Uri?, followup: ((Uri) -> Unit)?) {
        val job = Job()
        val uiScope = CoroutineScope(Dispatchers.IO + job)
        uiScope.launch {
            val fileUri = getFilePathFromUri(context, uri)
            val compressedImageFile = context?.let {
                fileUri?.path?.let { path ->
                    Compressor.compress(it, File(path)) {
                        quality(COMPRESS_QUALITY) // combine with compressor constraint
                        format(COMPRESS_FORMAT)
                    }
                }
            }
            val resultUri = Uri.fromFile(compressedImageFile)
            followup?.invoke(resultUri)
        }
    }

    private fun getFilePathFromUri(context: Context?, uri: Uri?): Uri? {
        val fileName: String = getFileName(context, uri)
        val file = File(context?.externalCacheDir, fileName)
        file.createNewFile()
        FileOutputStream(file).use { outputStream ->
            if (uri != null) {
                context?.contentResolver?.openInputStream(uri).use { inputStream ->
                    copyFile(inputStream, outputStream)
                    outputStream.flush()
                }
            }
        }
        return Uri.fromFile(file)
    }

    private fun getFileName(context: Context?, uri: Uri?): String {
        var fileName: String? = getFileNameFromCursor(context, uri)
        if (fileName == null) {
            val fileExtension: String? = getFileExtension(context, uri)
            fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
        } else if (!fileName.contains(".")) {
            val fileExtension: String? = getFileExtension(context, uri)
            fileName = "$fileName.$fileExtension"
        }
        return fileName
    }

    private fun getFileNameFromCursor(context: Context?, uri: Uri?): String? {
        val fileCursor: Cursor? = uri?.let {
            context?.contentResolver
                ?.query(
                    it, arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )
        }
        var fileName: String? = null
        if (fileCursor != null && fileCursor.moveToFirst()) {
            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex)
            }
        }
        fileCursor?.close()
        return fileName
    }

    private fun getFileExtension(context: Context?, uri: Uri?): String? {
        val fileType: String? = uri?.let { context?.contentResolver?.getType(it) }
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    private fun copyFile(input: InputStream?, output: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int?
        while (input?.read(buffer).also { read = it } != -1) {
            read?.let { output.write(buffer, 0, it) }
        }
    }

}