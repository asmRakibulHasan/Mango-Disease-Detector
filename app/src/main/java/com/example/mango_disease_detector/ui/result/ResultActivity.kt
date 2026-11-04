package com.example.mango_disease_detector.ui.result

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.databinding.ActivityResultBinding
import com.example.mango_disease_detector.databinding.ItemStepBinding
import com.example.mango_disease_detector.ui.weather.WeatherActivity
import com.example.mango_disease_detector.utils.BanglaFormat
import com.example.mango_disease_detector.utils.ImageUtils
import com.example.mango_disease_detector.utils.edgeToEdge
import com.example.mango_disease_detector.viewmodel.DetectorViewModel
import com.example.mango_disease_detector.viewmodel.UiState
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class ResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: DetectorViewModel by viewModels()

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var uploadFile: File? = null
    private var scanAnimator: ObjectAnimator? = null

    private var currentDiseaseName = ""
    private var currentConfidence = 0
    private var currentInfo: DiseaseInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tts = TextToSpeech(this, this)

        val uriString = intent.getStringExtra(EXTRA_IMAGE_URI) ?: run { finish(); return }
        val imageUri = Uri.parse(uriString)

        Glide.with(this).load(imageUri).into(binding.ivLeaf)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnTryAgain.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { retry(imageUri) }
        binding.btnWeather.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnSpeakResult.setOnClickListener { speakResult() }
        binding.btnSpeakAdvice.setOnClickListener { speakAdvice() }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Idle -> startPrediction(imageUri)
                is UiState.Loading -> showLoading()
                is UiState.Success -> showResult(state)
                is UiState.Error -> showError()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("bn", "BD"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    // ---------- Prediction ----------

    private fun startPrediction(uri: Uri) {
        val file = try {
            ImageUtils.uriToCompressedFile(this, uri)
        } catch (e: Exception) {
            showError()
            return
        }
        uploadFile = file
        viewModel.predict(file)
    }

    private fun retry(uri: Uri) {
        val file = uploadFile
        if (file != null && file.exists()) viewModel.predict(file) else startPrediction(uri)
    }

    // ---------- States ----------

    private fun showLoading() {
        binding.tvHeaderTitle.setText(R.string.result_analyzing_header)
        setPhotoHeight(300)
        binding.layoutScan.visibility = View.VISIBLE
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutResult.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        startScan()
        binding.stepActive.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
    }

    private fun showError() {
        stopScan()
        binding.tvHeaderTitle.setText(R.string.result_title)
        setPhotoHeight(210)
        binding.layoutScan.visibility = View.GONE
        binding.layoutLoading.visibility = View.GONE
        binding.layoutResult.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
    }

    private fun showResult(state: UiState.Success) {
        stopScan()
        binding.tvHeaderTitle.setText(R.string.result_title)
        setPhotoHeight(210)
        binding.layoutScan.visibility = View.GONE
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.layoutResult.visibility = View.VISIBLE
        binding.layoutResult.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        val info = DiseaseInfo.of(state.result.disease)
        currentInfo = info
        currentDiseaseName = info?.name ?: state.result.disease
        currentConfidence = state.result.confidence.roundToInt().coerceIn(0, 100)
        val healthy = info?.isHealthy == true

        // Banner
        binding.layoutBanner.setBackgroundColor(getColor(if (healthy) R.color.green_primary else R.color.red))
        binding.ivBanner.setImageResource(if (healthy) R.drawable.ic_check else R.drawable.ic_warning_line)
        binding.tvBanner.setText(if (healthy) R.string.result_banner_healthy else R.string.result_banner_disease)

        // Name + type
        binding.tvDiseaseName.text = currentDiseaseName
        binding.tvDiseaseName.setTextColor(getColor(if (healthy) R.color.green_primary else R.color.red_deep))
        binding.tvDiseaseType.text = info?.type.orEmpty()
        binding.tvDiseaseType.visibility = if (info != null) View.VISIBLE else View.GONE
        binding.tvDiseaseType.backgroundTintList =
            ColorStateList.valueOf(getColor(if (healthy) R.color.mint else R.color.butter))
        binding.tvDiseaseType.setTextColor(getColor(if (healthy) R.color.green_primary else R.color.amber_text))

        // Confidence
        binding.tvConfidence.text = BanglaFormat.digits(currentConfidence.toString()) + "%"
        binding.progressConfidence.progressTintList =
            ColorStateList.valueOf(getColor(if (healthy) R.color.leaf else R.color.orange))
        ObjectAnimator.ofInt(binding.progressConfidence, "progress", 0, currentConfidence)
            .setDuration(700).start()

        showAdvice(info, healthy)
    }

    private fun showAdvice(info: DiseaseInfo?, healthy: Boolean) {
        if (info == null) {
            binding.layoutAdvice.visibility = View.GONE
            return
        }
        binding.tvAdviceHeader.text = getString(
            if (healthy) R.string.result_care_header else R.string.result_advice_header,
            BanglaFormat.digits(info.steps.size.toString())
        )
        binding.layoutAdviceHeader.setBackgroundColor(getColor(if (healthy) R.color.mint_strong else R.color.mango))

        binding.layoutAdviceSteps.removeAllViews()
        info.steps.forEachIndexed { index, step ->
            val row = ItemStepBinding.inflate(layoutInflater, binding.layoutAdviceSteps, false)
            row.tvStepNumber.text = BanglaFormat.digits((index + 1).toString())
            row.stepTile.backgroundTintList = ColorStateList.valueOf(getColor(step.icon.tile))
            row.ivStepIcon.setImageResource(step.icon.icon)
            row.ivStepIcon.imageTintList = ColorStateList.valueOf(getColor(step.icon.tint))
            row.tvStepText.text = step.text
            binding.layoutAdviceSteps.addView(row.root)
            if (index < info.steps.lastIndex) {
                binding.layoutAdviceSteps.addView(View(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                    )
                    setBackgroundColor(getColor(R.color.beige))
                })
            }
        }
        binding.layoutAdvice.visibility = View.VISIBLE
    }

    private fun setPhotoHeight(dp: Int) {
        binding.cardPhoto.layoutParams = binding.cardPhoto.layoutParams.apply { height = dpToPx(dp) }
    }

    private fun startScan() {
        if (scanAnimator != null) return
        binding.cardPhoto.post {
            val travel = (binding.cardPhoto.height - binding.scanLine.height).toFloat()
            scanAnimator = ObjectAnimator.ofFloat(binding.scanLine, View.TRANSLATION_Y, 0f, travel).apply {
                duration = 1600
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    private fun stopScan() {
        scanAnimator?.cancel()
        scanAnimator = null
        binding.stepActive.clearAnimation()
    }

    // ---------- Voice ----------

    private fun speakResult() {
        if (!ttsReady) {
            Toast.makeText(this, getString(R.string.weather_voice_not_ready), Toast.LENGTH_SHORT).show()
            return
        }
        val text = "$currentDiseaseName। ${getString(R.string.result_confidence)} " +
                "${BanglaFormat.digits(currentConfidence.toString())} শতাংশ।"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "result")
    }

    private fun speakAdvice() {
        if (!ttsReady) {
            Toast.makeText(this, getString(R.string.weather_voice_not_ready), Toast.LENGTH_SHORT).show()
            return
        }
        val info = currentInfo ?: return
        val text = buildString {
            append(binding.tvAdviceHeader.text).append("। ")
            info.steps.forEachIndexed { i, step ->
                append(BanglaFormat.digits((i + 1).toString())).append("। ").append(step.text).append("। ")
            }
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "advice")
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        stopScan()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
