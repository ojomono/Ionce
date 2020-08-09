package com.ojomono.ionce.ui.roll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ojomono.ionce.R

class RollFragment : Fragment() {

    private lateinit var rollViewModel: RollViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rollViewModel =
            ViewModelProvider(this).get(RollViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_roll, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        rollViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }
}