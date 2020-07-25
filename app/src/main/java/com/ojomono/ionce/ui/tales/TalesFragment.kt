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
import kotlinx.android.synthetic.main.fragment_tales_list.view.*

/**
 * A fragment representing a list of Tales.
 */
class TalesFragment : Fragment(), TalesAdapter.TalesListener {

    private lateinit var talesViewModel: TalesViewModel
    private var columnCount = 1

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
        val root = inflater.inflate(R.layout.fragment_tales_list, container, false)

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

        // Set add fab action
        root.fab_add_tale.setOnClickListener { showNewDialog() }

        return root
    }

    private fun showNewDialog() {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp

        dialogBuilder.setView(input)

        dialogBuilder.setTitle("New Tale")
        dialogBuilder.setMessage("Enter Title Below")
        dialogBuilder.setPositiveButton("Save") { dialog, _ ->
            talesViewModel.createTale(input.text.toString())
            dialog.cancel()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        val b = dialogBuilder.create()
        b.show()
    }

    private fun showUpdateDialog(tale: Tale) {
        val dialogBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.setText(tale.title)

        dialogBuilder.setView(input)

        dialogBuilder.setTitle("Update Tale")
        dialogBuilder.setPositiveButton("Update") { dialog, _ ->
            talesViewModel.updateTale(tale.id, input.text.toString())
            dialog.cancel()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        val b = dialogBuilder.create()
        b.show()
    }

    private fun showDeleteDialog(tale: Tale) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Delete")
        dialogBuilder.setMessage("Confirm delete?")
        dialogBuilder.setPositiveButton("Delete") { dialog, _ ->
            talesViewModel.deleteTale(tale.id)
            dialog.cancel()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        val b = dialogBuilder.create()
        b.show()
    }

    override fun onEditClicked(tale: Tale) {
        showUpdateDialog(tale)
    }

    override fun onDeleteClicked(tale: Tale) {
        showDeleteDialog(tale)
    }

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