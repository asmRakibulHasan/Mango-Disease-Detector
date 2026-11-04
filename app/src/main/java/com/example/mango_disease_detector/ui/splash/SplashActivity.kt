package com.example.mango_disease_detector.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.databinding.ActivitySplashBinding
import com.example.mango_disease_detector.ui.home.HomeActivity
import com.example.mango_disease_detector.ui.onboarding.OnboardingActivity
import com.example.mango_disease_detector.utils.edgeToEdge

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge(lightIcons = true)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val onboardingDone = prefs.getBoolean("onboarding_complete", false)
            val next = if (onboardingDone) HomeActivity::class.java else OnboardingActivity::class.java
            startActivity(Intent(this, next))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 2000)
    }
}
