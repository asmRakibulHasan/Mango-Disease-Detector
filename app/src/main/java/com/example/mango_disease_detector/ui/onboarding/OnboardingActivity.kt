package com.example.mango_disease_detector.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.databinding.ActivityOnboardingBinding
import com.example.mango_disease_detector.ui.home.HomeActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    private val pages by lazy {
        listOf(
            OnboardingFragment.newInstance(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.ic_leaf
            ),
            OnboardingFragment.newInstance(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.ic_camera
            ),
            OnboardingFragment.newInstance(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.ic_leaf
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val adapter = OnboardingAdapter(this, pages)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                binding.btnNext.text = if (position == pages.lastIndex)
                    getString(R.string.btn_start)
                else
                    getString(R.string.btn_next)
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.tvSkip.setOnClickListener { finishOnboarding() }
    }

    private fun updateDots(position: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, dot ->
            dot.background = if (index == position)
                getDrawable(R.drawable.bg_onboarding_indicator_active)
            else
                getDrawable(R.drawable.bg_onboarding_indicator_inactive)
            val size = if (index == position) 12.dpToPx() else 8.dpToPx()
            dot.layoutParams = dot.layoutParams.apply {
                width = size
                height = size
            }
            dot.requestLayout()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun finishOnboarding() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putBoolean("onboarding_complete", true).apply()
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
