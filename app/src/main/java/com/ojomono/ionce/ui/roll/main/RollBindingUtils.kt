package com.ojomono.ionce.ui.roll.main

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.models.GroupModel
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.proxies.ImageLoader

const val TITLE_LINES_FOR_TALE_WITH_COVER = 2

@BindingAdapter("noTalesTextVisibility")
fun TextView.setNoTalesTextVisibility(tales: List<TaleItemModel>?) {
    visibility = if (tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("rollScreenVisibility")
fun LinearLayout.setRollScreenVisibility(tales: List<TaleItemModel>?) {
    visibility = if (!tales.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.VISIBLE else View.GONE
}

@BindingAdapter("rolledCardVisibility")
fun CardView.setRolledCardVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.GONE else View.VISIBLE
}

@BindingAdapter("titleTextLinesAndEllipsize")
fun TextView.setTitleTextLinesAndEllipsize(coverUri: String?) {
    ellipsize = if (coverUri.isNullOrEmpty()) {
        maxLines = Integer.MAX_VALUE
        null
    } else {
        setLines(TITLE_LINES_FOR_TALE_WITH_COVER)
        TextUtils.TruncateAt.END
    }
}

@BindingAdapter("coverSrcAndVisibility")
fun ImageView.setCoverSrcAndVisibility(coverUri: String?) {
    visibility = if (coverUri.isNullOrEmpty()) View.GONE
    else {
        ImageLoader.load(context, Uri.parse(coverUri), this)
        View.VISIBLE
    }
}

@BindingAdapter("rollButtonAppearance")
fun TextView.setRollButtonAppearance(group: GroupModel?) {
    if (group == null) {
        text = resources.getString(R.string.roll_button_text_default)
        background = getDrawable(context, R.drawable.roll_button_bg_default)
    } else {
        text = resources.getString(R.string.roll_button_text_group)
        background = getDrawable(context, R.drawable.roll_button_bg_group)
    }
}

@BindingAdapter("currentGameText")
fun Button.setCurrentGameText(group: GroupModel?) {
    text = resources.getString(
        if (group == null) R.string.roll_game_simple_roll else R.string.roll_game_group_roll
    )
}

@BindingAdapter("showOwnerLinearVisibility")
fun LinearLayout.setShowOwnerLinearVisibility(group: GroupModel?) {
    visibility = if (group != null) View.VISIBLE else View.GONE
}

@BindingAdapter("showOwnerText")
fun TextView.setShowOwnerText(shown: Boolean) {
    text = resources.getString(
        if (shown) R.string.roll_show_owner_shown else R.string.roll_show_owner_not_shown
    )
}

@BindingAdapter("ownerNameTextVisibility")
fun TextView.setOwnerNameTextVisibility(shown: Boolean) {
    visibility = if (shown) View.VISIBLE else View.GONE
}
