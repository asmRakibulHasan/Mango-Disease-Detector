package com.example.mango_disease_detector.ui.result

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.databinding.ActivityResultBinding
import com.example.mango_disease_detector.utils.ImageUtils
import com.example.mango_disease_detector.viewmodel.DetectorViewModel
import com.example.mango_disease_detector.viewmodel.UiState

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: DetectorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val uriString = intent.getStringExtra(EXTRA_IMAGE_URI) ?: run { finish(); return }
        val imageUri = Uri.parse(uriString)

        Glide.with(this).load(imageUri).into(binding.ivLeaf)

        binding.btnTryAgain.setOnClickListener { finish() }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Idle -> startPrediction(imageUri)
                is UiState.Loading -> showLoading()
                is UiState.Success -> showResult(state)
                is UiState.Error -> showError()
            }
        }
    }

    private fun startPrediction(uri: Uri) {
        val file = ImageUtils.uriToCompressedFile(this, uri)
        viewModel.predict(file)
    }

    private fun showLoading() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutResult.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showResult(state: UiState.Success) {
        binding.layoutLoading.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.layoutResult.visibility = View.VISIBLE
        binding.layoutResult.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))

        val bengaliName = diseaseNameMap[state.result.disease] ?: state.result.disease
        binding.tvDiseaseName.text = bengaliName
        binding.tvConfidence.text = "${getString(R.string.result_confidence)}: ${"%.1f".format(state.result.confidence)}%"

        val isHealthy = state.result.disease.equals("Healthy", ignoreCase = true)
        binding.tvDiseaseName.setTextColor(
            getColor(if (isHealthy) R.color.healthy_color else R.color.disease_color)
        )
    }

    private fun showError() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutResult.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"

        val diseaseNameMap = mapOf(
            "Anthracnose" to "অ্যানথ্রাকনোজ",
            "Bacterial Canker" to "ব্যাকটেরিয়াল ক্যাংকার",
            "Cutting Weevil" to "কাটিং উইভিল",
            "Die Back" to "ডাই ব্যাক",
            "Gall Midge" to "গল মিজ",
            "Healthy" to "সুস্থ ✅",
            "Powdery Mildew" to "পাউডারি মিলডিউ",
            "Sooty Mould" to "সুটি মোল্ড"
        )
    }
}
