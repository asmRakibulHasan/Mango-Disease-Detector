package com.example.mango_disease_detector.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingAdapter(
    activity: FragmentActivity,
    private val pages: List<OnboardingFragment>
) : FragmentStateAdapter(activity) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int): Fragment = pages[position]
}
