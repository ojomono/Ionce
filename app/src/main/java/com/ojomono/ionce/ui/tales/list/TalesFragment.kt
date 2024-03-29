package com.ojomono.ionce.ui.tales.list

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
import com.ojomono.ionce.ui.tales.edit.EditTaleDialogFragment
import com.ojomono.ionce.utils.*
import com.ojomono.ionce.ui.bases.BaseFragment
import com.ojomono.ionce.ui.bases.BaseViewModel
import com.ojomono.ionce.utils.proxies.DialogShower

/**
 * A fragment representing a list of Tales.
 * Use the [TalesFragment.newInstance] factory method to create an instance of this fragment.
 */
class TalesFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_tales
    override lateinit var binding: FragmentTalesBinding
    override lateinit var viewModel: TalesViewModel
    override lateinit var progressBar: ProgressBar

    // Give default value for the column count
    private var columnCount = DEFAULT_COLUMN_COUNT

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
        savedInstanceState: Bundle?,
    ): View {
        viewModel = ViewModelProvider(this).get(TalesViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        progressBar = binding.progressBar
        observeEvents()

        // Listen to the toggle button at the top, to know what list to show
        initToggleButton()

        // Populate the recycler view
        populateRecyclerView()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.BaseEventType) {
        super.handleEvent(event)
        when (event) {
            is TalesViewModel.EventType.ShowEditTaleDialog -> showEditTaleDialog(event.taleId)
            is TalesViewModel.EventType.ShowDeleteTaleDialog ->
                showDeleteTaleDialog(event.taleTitle)
        }
    }

    /*********************/
    /** private methods **/
    /*********************/

    /**
     * Set the viewModel's LiveData to reflect the chosen toggle button.
     */
    private fun initToggleButton() {
        viewModel.setShownList(viewIdToListType(binding.toggleButton.checkedButtonId))
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) viewModel.setShownList(viewIdToListType(checkedId))
        }
    }

    // TODO This is a workaround because the ViewModel can't access the view id.
    //  Find a better solution!
    private fun viewIdToListType(viewId: Int) = when (viewId) {
        binding.buttonMyTales.id -> TalesViewModel.ListType.MY_TALES
        binding.buttonHeardTales.id -> TalesViewModel.ListType.HEARD_TALES
        else -> TalesViewModel.ListType.MY_TALES    // Default to my tales
    }


    /**
     * Populate the recycler view.
     */
    // TODO move to common ListFragment Interface/abstract class
    private fun populateRecyclerView() {
        with(binding.recyclerTalesList) {
            // Choose layout manager
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> buildGridLayoutManager()
            }

            // Init the adapter
            val talesAdapter =
                TalesListAdapter(
                    getString(R.string.tales_header_text),
                    viewModel,
                    viewModel.shownList
                ).also { adapter = it }

            // Observe changes in tales list
            viewModel.shownTales.observe(viewLifecycleOwner, {
                it?.let { talesAdapter.addHeaderAndSubmitList(it) }
            })
        }
    }

    /**
     * Build a [GridLayoutManager] using the wanted [columnCount] but expending header to span
     * across the hole screen.
     */
    // TODO move to common ListFragment Interface/abstract class
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
     * Show dialog for editing the tale with id [taleId].
     */
    private fun showEditTaleDialog(taleId: String) =
        EditTaleDialogFragment.newInstance(taleId).let { it.show(parentFragmentManager, it.TAG) }

    /**
     * Show dialog for deleting the tale with title [taleTitle].
     */
    private fun showDeleteTaleDialog(taleTitle: String) =
        DialogShower.show(context) {
            message(text = getString(R.string.tales_delete_dialog_message, taleTitle))
            positiveButton(R.string.tales_delete_dialog_positive_button_text) { viewModel.deleteTale() }
            negativeButton(R.string.dialog_cancel) { viewModel.clearClickedTale() }
        }

    /**********************/
    /** Companion object **/
    /**********************/

    companion object {

        // Column count
        const val ARG_COLUMN_COUNT = "column-count"
        const val DEFAULT_COLUMN_COUNT = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment with [columnCount] columns.
         */
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TalesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}