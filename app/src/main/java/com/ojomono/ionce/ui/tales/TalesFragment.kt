package com.ojomono.ionce.ui.tales

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.models.TalesItem
import kotlinx.android.synthetic.main.fragment_tales.view.*

/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : Fragment(), TalesAdapter.TalesListener {

    private lateinit var talesViewModel: TalesViewModel
    private var columnCount = 1

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        talesViewModel = ViewModelProvider(this).get(TalesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tales, container, false)

        // Set the adapter
        val adapter = TalesAdapter(this)
        with(root.recycler_tales_list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            this.adapter = adapter
            talesViewModel.tales.observe(viewLifecycleOwner, Observer {
                it?.let { adapter.submitList(it) }
            })
        }

        // TODO: Use binding and event wrapper pattern to set click listeners:
        // https://codelabs.developers.google.com/codelabs/kotlin-android-training-live-data-data-binding/index.html?index=..%2F..android-kotlin-fundamentals#3
        // https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
        // Set add fab action
        root.fab_add_tale.setOnClickListener { showAddDialog() }

        return root
    }

    /****************************************/
    /** TalesAdapter.TalesListener methods **/
    /****************************************/

    override fun onEditTaleClicked(talesItem: TalesItem) {
        showUpdateDialog(talesItem)
    }

    override fun onDeleteTaleClicked(talesItem: TalesItem) {
        showDeleteDialog(talesItem)
    }

    /*************************/
    /** Show dialog methods **/
    /*************************/

    private fun showAddDialog() {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.tales_add_dialog_title))
        dialogBuilder.setMessage(getText(R.string.tales_add_dialog_message))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_add_dialog_positive_button_text))
            { dialog, _ ->
                talesViewModel.addTale(input.text.toString())
                dialog.cancel()
            }
        dialogBuilder
            .setNegativeButton(getText(R.string.tales_dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }
        val b = dialogBuilder.create()
        b.show()
    }

    private fun showUpdateDialog(talesItem: TalesItem) {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(talesItem.title)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.tales_update_dialog_title))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_update_dialog_positive_button_text))
            { dialog, _ ->
                talesViewModel.updateTale(talesItem.id, input.text.toString())
                dialog.cancel()
            }
        dialogBuilder
            .setNegativeButton(getText(R.string.tales_dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }
        val b = dialogBuilder.create()
        b.show()
    }

    private fun showDeleteDialog(talesItem: TalesItem) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(getText(R.string.tales_delete_dialog_title))
        dialogBuilder.setMessage(getText(R.string.tales_delete_dialog_message))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_delete_dialog_positive_button_text))
            { dialog, _ ->
                talesViewModel.deleteTale(talesItem.id)
                dialog.cancel()
            }
        dialogBuilder
            .setNegativeButton(getText(R.string.tales_dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }
        val b = dialogBuilder.create()
        b.show()
    }

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TalesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}