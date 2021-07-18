package com.ojomono.ionce.ui.roll.group

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.GroupModel
import com.ojomono.ionce.utils.ImageUtils


@BindingAdapter("group", "forExisting")
fun LinearLayout.setGroupRollScreenVisibility(group: GroupModel?, forExisting: Boolean) {
    val exists = group != null
    visibility = if (exists == forExisting) View.VISIBLE else View.GONE
}

@BindingAdapter("groupQRCodeBitmap")
fun ImageView.setGroupQRCodeBitmap(group: GroupModel?) {
    val imageBitmap =
        if (group != null) ImageUtils.generateQRCode(group.id)
        else Bitmap.createBitmap(
            ImageUtils.QRCODE_SIZE,
            ImageUtils.QRCODE_SIZE,
            Bitmap.Config.ARGB_8888
        )

    setImageBitmap(imageBitmap)
}
