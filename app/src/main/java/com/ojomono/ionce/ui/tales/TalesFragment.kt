package com.ojomono.ionce.ui.tales

import android.app.AlertDialog
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentTalesBinding
import com.ojomono.ionce.utils.*
import kotlinx.android.synthetic.main.fragment_tales.progress_bar
import kotlinx.android.synthetic.main.fragment_tales.view.*

/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_tales
    override lateinit var binding: FragmentTalesBinding
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
        viewModel = ViewModelProvider(this).get(TalesViewModel::class.java)
        binding = getDataBinding(inflater, container)
        binding.viewModel = viewModel
        observeEvents()

        // Init the adapter
        initAdapter()

        return binding.root
    }

    /**************************/
    /** BaseFragment methods **/
    /**************************/

    override fun handleEvent(event: BaseViewModel.Event) {
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
     * Show dialog for adding a new tale.
     */
    private fun showAddTaleDialog() =
        AlertDialog.Builder(context)
            .setTitle(R.string.tales_add_dialog_title)
            .setMessage(R.string.tales_add_dialog_message)
            .setInputAndSaveButton(viewModel::addTale, progress_bar)
            .setCancelButton()
            .create()
            .show()

    /**
     * Show dialog for updating the tale with title [taleTitle].
     */
    private fun showUpdateTaleDialog(taleTitle: String) =
        AlertDialog.Builder(context)
            .setTitle(R.string.tales_update_dialog_title)
            .setInputAndSaveButton(viewModel::updateTale, progress_bar, taleTitle)
            .setCancelButton(viewModel::clearClickedTale)
            .create()
            .show()

    /**
     * Show dialog for deleting the tale with title [taleTitle].
     */
    private fun showDeleteTaleDialog(taleTitle: String) =
        AlertDialog.Builder(context)
            .setTitle(R.string.tales_delete_dialog_title)
            .setMessage(getString(R.string.tales_delete_dialog_message, taleTitle))
            .setPositiveButton(getText(R.string.tales_delete_dialog_positive_button_text))
            { dialog, _ ->
                viewModel.deleteTale()?.withProgressBar(progress_bar)
                dialog.cancel()
            }
            .setCancelButton(viewModel::clearClickedTale)
            .create()
            .show()

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