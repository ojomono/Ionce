package com.ojomono.ionce.utils.bases

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.StringResource

/**
 * A full-screen [DialogFragment].
 */
abstract class FullScreenDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set style to be app theme (instead of default dialog theme) to make full-screen dialog
        // (actually the important thing is: <item name="android:windowIsFloating">false</item>)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the enter & exit sliding animations
        dialog?.window?.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
    }

    override fun onPause() {
        super.onPause()

        // Disable enter animation for this instance to avoid animation on back from image picker
        dialog?.window?.setWindowAnimations(R.style.AppTheme_FullScreenDialog_NoEnterAnim)
    }

    override fun dismiss() {
        // Hide on-screen soft keyboard (if shown) before dismissing the dialog.
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view?.windowToken, 0)

        // Dismiss dialog
        super.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // handle close button click here
                dismiss()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Set the action bar with the given [toolbar].
     */
    protected open fun setActionBar(
        toolbar: Toolbar,
        title: StringResource = StringResource.EMPTY
    ) {
        // If a title was sent, set it as toolbar title
        if (title != StringResource.EMPTY) toolbar.title = context?.let { title.inContext(it) }

        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)

        val actionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
        }
        setHasOptionsMenu(true)
    }

}