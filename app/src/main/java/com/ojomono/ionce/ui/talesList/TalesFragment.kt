package com.ojomono.ionce.ui.talesList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentTalesBinding
import com.ojomono.ionce.ui.dialogs.InputDialogFragment
import com.ojomono.ionce.ui.dialogs.AlertDialogFragment
import com.ojomono.ionce.utils.*
import kotlinx.android.synthetic.main.fragment_tales.view.*


/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_tales
    override lateinit var binding: FragmentTalesBinding
    override lateinit var viewModel: TalesViewModel
    override lateinit var progressBar: ProgressBar
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
    ): View {
        viewModel = ViewModelProvider(this).get(TalesViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        progressBar = binding.progressBar
        observeEvents()

        // Populate the recycler view
        populateRecyclerView()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
        super.handleEvent(event)
        when (event) {
            is TalesViewModel.EventType.ShowAddTaleDialog -> showAddTaleDialog()
            is TalesViewModel.EventType.ShowEditTaleDialog -> showUpdateTaleDialog(event.taleTitle)
            is TalesViewModel.EventType.ShowDeleteTaleDialog -> showDeleteTaleDialog(event.taleTitle)
        }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Populate the recycler view.
     */
    private fun populateRecyclerView() {
        with(binding.root.recycler_tales_list) {
            // Choose layout manager
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> buildGridLayoutManager()
            }

            // Init the adapter
            val talesAdapter = TalesAdapter(viewModel)
            adapter = talesAdapter

            // For drag n' drop feature
//            // Add  Item touch helper to the recycler view
//            val itemTouchHelper = ItemTouchHelper(TalesTouchCallback(talesAdapter))
//            itemTouchHelper.attachToRecyclerView(this)

            // Observe changes in tales list
            viewModel.tales.observe(viewLifecycleOwner, {
                it?.let { talesAdapter.addHeaderAndSubmitList(it) }
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
     * Show dialog for adding a new tale.
     */
    private fun showAddTaleDialog() =
        InputDialogFragment(
            title = StringResource(R.string.tales_add_dialog_title),
            message = StringResource(R.string.tales_header_text),
            onPositive = viewModel::addTale
        ).show(parentFragmentManager, FT_ADD)

    /**
     * Show dialog for updating the tale with title [taleTitle].
     */
    private fun showUpdateTaleDialog(taleTitle: String) =
        InputDialogFragment(
            title = StringResource(R.string.tales_update_dialog_title),
            message = StringResource(R.string.tales_header_text),
            onNegative = viewModel::clearClickedTale,
            onPositive = viewModel::updateTale,
            defaultInputText = StringResource(taleTitle)
        ).show(parentFragmentManager, FT_UPDATE)

    /**
     * Show dialog for deleting the tale with title [taleTitle].
     */
    private fun showDeleteTaleDialog(taleTitle: String) =
        AlertDialogFragment(
            message = StringResource(getString(R.string.tales_delete_dialog_message, taleTitle)),
            onNegative = viewModel::clearClickedTale,
            onPositive = viewModel::deleteTale,
            okButtonText = StringResource(R.string.tales_delete_dialog_positive_button_text)
        ).show(parentFragmentManager, FT_DELETE)

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // Fragment tags
        const val FT_ADD = "add"
        const val FT_UPDATE = "update"
        const val FT_DELETE = "delete"

        // Column count
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