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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentTalesBinding
import com.ojomono.ionce.models.TalesItem
import kotlinx.android.synthetic.main.fragment_tales.view.*

/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : Fragment() {

    private lateinit var binding: FragmentTalesBinding
    private lateinit var viewModel: TalesViewModel
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
        viewModel = ViewModelProvider(this).get(TalesViewModel::class.java)

        // Inflate view and obtain an instance of the binding class
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_tales,
                container,
                false
            )

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.talesViewModel = viewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        // Set the adapter
        val adapter = TalesAdapter(viewModel)
        with(binding.root.recycler_tales_list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            this.adapter = adapter
            viewModel.tales.observe(viewLifecycleOwner, Observer {
                it?.let { adapter.submitList(it) }
            })
        }

        // Observe the view model for events - open the right dialog for the event
        viewModel.itemEvent.observe(viewLifecycleOwner, Observer {
            it.consume { event -> showEventDialog(event) }
        })

        return binding.root
    }

    /*************************/
    /** Show dialog methods **/
    /*************************/

    private fun showEventDialog(event: TalesViewModel.EventType) {

        // Build the right dialog UI
        val dialogBuilder = when (event) {
            is TalesViewModel.EventType.AddItemEvent -> buildAddDialog(event.onOk)
            is TalesViewModel.EventType.UpdateItemEvent -> buildUpdateDialog(event.onOk, event.item)
            is TalesViewModel.EventType.DeleteItemEvent -> buildDeleteDialog(event.onOk, event.item)
        }

        // Add the 'cancel' button
        dialogBuilder
            .setNegativeButton(getText(R.string.tales_dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }

        // Create the dialog and show it
        dialogBuilder.create().show()
    }

    private fun buildAddDialog(onOk: (item: TalesItem) -> Unit): AlertDialog.Builder {
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
                onOk(TalesItem(title = input.text.toString()))
                dialog.cancel()
            }
        return dialogBuilder
    }

    private fun buildUpdateDialog(onOk: (item: TalesItem) -> Unit, item: TalesItem)
            : AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(item.title)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.tales_update_dialog_title))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_update_dialog_positive_button_text))
            { dialog, _ ->
                item.title = input.text.toString()
                onOk(item)
                dialog.cancel()
            }
        return dialogBuilder
    }

    private fun buildDeleteDialog(onOk: (item: TalesItem) -> Unit, item: TalesItem)
            : AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(getText(R.string.tales_delete_dialog_title))
        dialogBuilder.setMessage(getText(R.string.tales_delete_dialog_message))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_delete_dialog_positive_button_text))
            { dialog, _ ->
                onOk(item)
                dialog.cancel()
            }
        return dialogBuilder
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