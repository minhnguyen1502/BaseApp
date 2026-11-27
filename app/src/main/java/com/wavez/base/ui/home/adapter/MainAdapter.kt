package com.wavez.base.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.home.fragment.HomeFragment
import com.wavez.base.ui.home.fragment.SettingFragment

class MainAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            HomeActivity.PAGE_HOME -> HomeFragment()
            HomeActivity.PAGE_SETTING -> SettingFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
