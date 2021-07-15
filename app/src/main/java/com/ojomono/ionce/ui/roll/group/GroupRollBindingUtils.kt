package com.ojomono.ionce.ui.roll.group

import android.view.View
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.models.GroupModel

@BindingAdapter("noGroupLinearVisibility")
fun LinearLayout.setNoGroupLinearVisibility(group: GroupModel?) {
    visibility = if (group == null) View.VISIBLE else View.GONE
}
