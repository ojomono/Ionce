package com.ojomono.ionce.ui.roll.main

import android.net.Uri
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.ojomono.ionce.R
import com.ojomono.ionce.models.GroupModel
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.models.UserItemModel
import com.ojomono.ionce.utils.proxies.ImageLoader

const val TITLE_LINES_FOR_TALE_WITH_COVER = 2

@BindingAdapter("tales", "group")
fun TextView.setNoTalesTextVisibility(tales: List<TaleItemModel>?, group: GroupModel?) {
    visibility = if (tales.isNullOrEmpty() and (group == null)) View.VISIBLE else View.GONE
}

@BindingAdapter("tales", "group")
fun LinearLayout.setRollScreenVisibility(tales: List<TaleItemModel>?, group: GroupModel?) {
    visibility = if (!tales.isNullOrEmpty() or (group != null)) View.VISIBLE else View.GONE
}

@BindingAdapter("hintTextVisibility")
fun TextView.setHintTextVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.VISIBLE else View.GONE
}

@BindingAdapter("tales", "rolledTale", "showResult")
fun CardView.setRolledCardVisibilityAndTint(
    tales: List<TaleItemModel>?,
    rolledTale: TaleItemModel?,
    showResult: Boolean,
) {
    visibility = if (rolledTale == null) View.GONE else View.VISIBLE

    background.setTintList(null)
    if (showResult) if (tales != null) if (rolledTale != null) {
        val colorRes = if (rolledTale in tales) R.color.roll_tal_result_truth
        else R.color.roll_tal_result_lie

        background.setTint(ContextCompat.getColor(context, colorRes))
    }
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

@BindingAdapter("coverUri", "currentGame")
fun ImageView.setCoverSrcAndVisibility(coverUri: String?, game: RollViewModel.Game?) {
    visibility =
        if (coverUri.isNullOrEmpty() or (game == RollViewModel.Game.TRUTH_AND_LIE)) View.GONE
        else {
            ImageLoader.load(context, Uri.parse(coverUri), this)
            View.VISIBLE
        }
}

@BindingAdapter("rollButtonAppearance")
fun TextView.setRollButtonAppearance(game: RollViewModel.Game?) {
    when (game) {
        RollViewModel.Game.ROLL ->
            setTextAndBg(R.string.roll_button_text_default, R.drawable.roll_button_bg_default)
        RollViewModel.Game.GROUP_ROLL ->
            setTextAndBg(R.string.roll_button_text_group, R.drawable.roll_button_bg_group)
        RollViewModel.Game.TRUTH_AND_LIE ->
            setTextAndBg(R.string.roll_button_text_tal, R.drawable.roll_button_bg_tal)
        null -> // Default to simple roll
            setTextAndBg(R.string.roll_button_text_default, R.drawable.roll_button_bg_default)
    }
}

fun TextView.setTextAndBg(stringRes: Int, DrawableRes: Int) {
    text = resources.getString(stringRes)
    background = getDrawable(context, DrawableRes)
}

@BindingAdapter("showOwnerLinearVisibility")
fun LinearLayout.setShowOwnerLinearVisibility(owner: UserItemModel?) {
    visibility = if (owner != null) View.VISIBLE else View.GONE
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

@BindingAdapter("resultButtonVisibility")
fun Button.setResultButtonVisibility(rolledTale: TaleItemModel?) {
    visibility = if (rolledTale == null) View.GONE else View.VISIBLE
}