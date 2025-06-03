package com.example.tomatoleafcare.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tomatoleafcare.ui.home.CardSlide1Fragment
import com.example.tomatoleafcare.ui.home.CardSlide2Fragment

class SliderAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CardSlide1Fragment()
            1 -> CardSlide2Fragment()
            else -> CardSlide1Fragment()
        }
    }
}
