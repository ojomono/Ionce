package com.ojomono.ionce.ui.tales

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R

/**
 * A fragment representing a list of Items.
 */
class TalesListFragment : Fragment() {

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
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_tales_list)
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            talesViewModel.tales.observe(viewLifecycleOwner, Observer {
                adapter = MyTaleRecyclerViewAdapter(it)
            })
        }

        return root
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TalesListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}