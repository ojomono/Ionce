package com.ojomono.ionce.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.R
import com.ojomono.ionce.ui.splashscreen.SplashActivity
import com.ojomono.ionce.utils.FirebaseProxy


class ProfileFragment : Fragment(), OnCompleteListener<Void> {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val textView: TextView = root.findViewById(R.id.text_profile)
        profileViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        // When clicked, sign out user and listen for completion
        val button: Button = root.findViewById(R.id.button_sign_out)
        button.setOnClickListener {
            context?.let { FirebaseProxy.signOut(it, this) }
        }

        return root
    }

    override fun onComplete(task: Task<Void>) {
        // User is now signed out - go back to splash screen
        startActivity(Intent(context, SplashActivity::class.java))
        activity?.finish()
    }

}