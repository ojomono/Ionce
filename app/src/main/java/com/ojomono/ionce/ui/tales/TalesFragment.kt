package com.ojomono.ionce.ui.tales

import android.app.AlertDialog
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentTalesBinding
import com.ojomono.ionce.models.TaleItemModel
import com.ojomono.ionce.utils.BaseFragment
import com.ojomono.ionce.utils.BaseViewModel
import com.ojomono.ionce.utils.withProgressBar
import kotlinx.android.synthetic.main.fragment_tales.*
import kotlinx.android.synthetic.main.fragment_tales.view.*

/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : BaseFragment() {

    private val layoutId = R.layout.fragment_tales
    private lateinit var binding: FragmentTalesBinding
    override lateinit var viewModel: TalesViewModel
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
        // Init the ViewModel
        viewModel = ViewModelProvider(this).get(TalesViewModel::class.java)

        // Init the DataBinding
        initDataBinding(inflater, container)

        // Init the adapter
        initAdapter()

        // Observe possible events
        observeEvents()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        // Build the right dialog UI
        val dialogBuilder = when (event) {
            is TalesViewModel.EventType.ShowAddTaleDialog -> buildAddDialog()
            is TalesViewModel.EventType.ShowEditTaleDialog -> buildUpdateDialog(event.taleItem)
            is TalesViewModel.EventType.ShowDeleteTaleDialog -> buildDeleteDialog(event.id)
            else -> null
        }

        // Add the 'cancel' button
        dialogBuilder
            ?.setNegativeButton(getText(R.string.dialogs_negative_button_text))
            { dialog, _ -> dialog.cancel() }

        // Create the dialog and show it
        dialogBuilder?.create()?.show()
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Init the [binding] field.
     */
    private fun initDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the model in the VieWModel
        binding.viewModel = viewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner
    }

    /**
     * Init the [binding] field.
     */
    private fun initAdapter() {
        val adapter = TalesAdapter(viewModel)
        with(binding.root.recycler_tales_list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> buildGridLayoutManager()
            }
            this.adapter = adapter
            viewModel.tales.observe(viewLifecycleOwner, {
                it?.let { adapter.addHeaderAndSubmitList(it) }
            })
        }
    }

    /**
     * Build a [GridLayoutManager] using the wanted [columnCount] but expending header to span
     * across the hole screen.
     */
    private fun buildGridLayoutManager(): GridLayoutManager {
        val manager = GridLayoutManager(context, columnCount)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = when (position) {
                0 -> columnCount
                else -> 1
            }
        }
        return manager
    }

    /**
     * Build a dialog builder for adding a new tale.
     */
    private fun buildAddDialog(): AlertDialog.Builder {
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
            .setPositiveButton(getText(R.string.dialogs_positive_button_text))
            { dialog, _ ->
                viewModel.addTale(input.text.toString())?.withProgressBar(progress_bar)
                dialog.cancel()
            }
        return dialogBuilder
    }

    /**
     * Build a dialog builder for updating the given tale [taleItem].
     */
    private fun buildUpdateDialog(taleItem: TaleItemModel): AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(taleItem.title)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle(getText(R.string.tales_update_dialog_title))
        dialogBuilder
            .setPositiveButton(getText(R.string.dialogs_positive_button_text))
            { dialog, _ ->
                // If we change original item, adapter's new list and old list would be the same and
                // it will not refresh. Thus a copy is needed.
                viewModel.updateTale(taleItem.copy(title = input.text.toString()))
                    ?.withProgressBar(progress_bar)
                dialog.cancel()
            }
        return dialogBuilder
    }

    /**
     * Build a dialog builder for deleting tale with the given [id].
     */
    private fun buildDeleteDialog(id: String): AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(getText(R.string.tales_delete_dialog_title))
        dialogBuilder.setMessage(getText(R.string.tales_delete_dialog_message))
        dialogBuilder
            .setPositiveButton(getText(R.string.tales_delete_dialog_positive_button_text))
            { dialog, _ ->
                viewModel.deleteTale(id)?.withProgressBar(progress_bar)
                dialog.cancel()
            }
        return dialogBuilder
    }

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            TalesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}