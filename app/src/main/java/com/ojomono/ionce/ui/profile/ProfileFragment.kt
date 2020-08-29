package com.ojomono.ionce.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.R
import com.ojomono.ionce.databinding.FragmentProfileBinding
import com.ojomono.ionce.databinding.FragmentRollBinding
import com.ojomono.ionce.ui.splashscreen.SplashActivity
import com.ojomono.ionce.firebase.Authentication


class ProfileFragment : Fragment(), OnCompleteListener<Void> {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    /***********************/
    /** Lifecycle methods **/
    /***********************/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )

        // Set the viewmodel for databinding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.profileViewModel = viewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner


        // Observe if an event was thrown
        viewModel.event.observe(
            viewLifecycleOwner,
            Observer {
                it.consume { signOutFun -> context?.let { context -> signOutFun(context, this) } }
            }
        )

        return binding.root
    }

    /**************************************/
    /** OnCompleteListener<Void> methods **/
    /**************************************/

    override fun onComplete(task: Task<Void>) {
        // User is now signed out - go back to splash screen
        startActivity(Intent(context, SplashActivity::class.java))
        activity?.finish()
    }

}