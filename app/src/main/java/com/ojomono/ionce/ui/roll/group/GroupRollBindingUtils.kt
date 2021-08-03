package com.ojomono.ionce.ui.roll.group

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.GroupModel
import com.ojomono.ionce.utils.proxies.QRCodeGenerator


@BindingAdapter("group", "forExisting")
fun LinearLayout.setGroupRollScreenVisibility(group: GroupModel?, forExisting: Boolean) {
    val exists = group != null
    visibility = if (exists == forExisting) View.VISIBLE else View.GONE
}

@BindingAdapter("group", "forExisting")
fun ScrollView.setGroupRollScreenVisibility(group: GroupModel?, forExisting: Boolean) {
    val exists = group != null
    visibility = if (exists == forExisting) View.VISIBLE else View.GONE
}

@BindingAdapter("groupQRCodeBitmap")
fun ImageView.setGroupQRCodeBitmap(group: GroupModel?) {
    val imageBitmap =
        if (group != null) QRCodeGenerator.generate(group.id)
        else Bitmap.createBitmap(
            QRCodeGenerator.QR_CODE_SIZE,
            QRCodeGenerator.QR_CODE_SIZE,
            Bitmap.Config.ARGB_8888
        )

    setImageBitmap(imageBitmap)
}
