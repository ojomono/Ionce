package com.ojomono.ionce.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ojomono.ionce.ui.profile.ProfileFragment
import com.ojomono.ionce.ui.roll.RollFragment
import com.ojomono.ionce.ui.talesList.TalesFragment

class MainFragmentPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> RollFragment()
            1 -> TalesFragment()
            2 -> ProfileFragment()
            else -> RollFragment()
        }
    }

    override fun getCount() = 3
}