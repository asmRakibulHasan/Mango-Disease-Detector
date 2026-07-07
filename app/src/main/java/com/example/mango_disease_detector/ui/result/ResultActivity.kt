package com.example.mango_disease_detector.ui.result

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import java.util.Locale

data class AdviceStep(val emoji: String, val text: String)
data class DiseaseAdvice(val headerTitle: String, val isHealthy: Boolean, val steps: List<AdviceStep>)

class ResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityResultBinding
    private val viewModel: DetectorViewModel by viewModels()
    private val bengaliNumerals = listOf("১", "২", "৩", "৪", "৫")

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentDiseaseName = ""
    private var currentConfidenceText = ""
    private var currentAdvice: DiseaseAdvice? = null

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

        tts = TextToSpeech(this, this)

        val uriString = intent.getStringExtra(EXTRA_IMAGE_URI) ?: run { finish(); return }
        val imageUri = Uri.parse(uriString)

        Glide.with(this).load(imageUri).into(binding.ivLeaf)

        binding.btnTryAgain.setOnClickListener { finish() }
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

    private fun speakResult() {
        if (!ttsReady) {
            Toast.makeText(this, "ভয়েস সাপোর্ট প্রস্তুত নয়", Toast.LENGTH_SHORT).show()
            return
        }
        tts?.speak("$currentDiseaseName। $currentConfidenceText।", TextToSpeech.QUEUE_FLUSH, null, "result")
    }

    private fun speakAdvice() {
        if (!ttsReady) {
            Toast.makeText(this, "ভয়েস সাপোর্ট প্রস্তুত নয়", Toast.LENGTH_SHORT).show()
            return
        }
        val advice = currentAdvice ?: return
        val sb = StringBuilder()
        sb.append(stripEmojis(advice.headerTitle))
        sb.append("। ")
        advice.steps.forEachIndexed { i, step ->
            sb.append("${i + 1}। ${step.text}। ")
        }
        tts?.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, "advice")
    }

    private fun stripEmojis(text: String): String =
        text.filter { c ->
            c.code in 0x0020..0x007E ||
            c.code in 0x0980..0x09FF ||
            c.code == 0x0964 ||
            c.code == 0x0965
        }.trim()

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
        currentDiseaseName = bengaliName
        currentConfidenceText = "${getString(R.string.result_confidence)} ${"%.1f".format(state.result.confidence)} শতাংশ"

        binding.tvDiseaseName.text = bengaliName
        binding.tvConfidence.text = "${getString(R.string.result_confidence)}: ${"%.1f".format(state.result.confidence)}%"

        val isHealthy = state.result.disease.equals("Healthy", ignoreCase = true)
        binding.tvDiseaseName.setTextColor(
            getColor(if (isHealthy) R.color.healthy_color else R.color.disease_color)
        )

        showAdvice(state.result.disease)
    }

    private fun showAdvice(disease: String) {
        val advice = diseaseAdviceMap[disease] ?: return
        currentAdvice = advice

        binding.tvAdviceHeader.text = advice.headerTitle
        binding.tvAdviceHeader.setTextColor(
            getColor(if (advice.isHealthy) R.color.healthy_color else R.color.yellow_accent)
        )

        binding.layoutAdviceSteps.removeAllViews()
        advice.steps.forEachIndexed { index, step ->
            binding.layoutAdviceSteps.addView(createStepView(index + 1, step.emoji, step.text, advice.isHealthy))
        }

        binding.layoutAdvice.visibility = View.VISIBLE
        binding.layoutAdvice.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
    }

    private fun createStepView(number: Int, emoji: String, stepText: String, isHealthy: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(14) }
        }

        val circleColor = getColor(if (isHealthy) R.color.green_primary else R.color.yellow_accent)
        val circleSize = dpToPx(44)

        val numView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(circleSize, circleSize)
            gravity = Gravity.CENTER
            setText(bengaliNumerals[number - 1])
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(getColor(R.color.white))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(circleColor)
            }
        }

        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).also { it.marginStart = dpToPx(12) }
            setText("$emoji  $stepText")
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
            setLineSpacing(0f, 1.2f)
        }

        row.addView(numView)
        row.addView(textView)
        return row
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun showError() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutResult.visibility = View.GONE
        binding.layoutAdvice.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
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

        val diseaseAdviceMap = mapOf(
            "Anthracnose" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("🍂", "আক্রান্ত পাতা ও ফল গাছ থেকে তুলে ফেলুন"),
                    AdviceStep("🔥", "তুলে ফেলা পাতা মাটিতে পুঁতে বা পুড়িয়ে দিন"),
                    AdviceStep("💊", "ম্যানকোজেব বা কপার অক্সিক্লোরাইড স্প্রে করুন"),
                    AdviceStep("🔁", "প্রতি ১৫ দিনে একবার স্প্রে করুন"),
                    AdviceStep("💧", "গাছের গোড়ায় পানি জমতে দেবেন না")
                )
            ),
            "Bacterial Canker" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("✂️", "আক্রান্ত ডাল ও পাতা কেটে সরিয়ে দিন"),
                    AdviceStep("🟤", "কাটা জায়গায় বোর্দো পেস্ট লাগান"),
                    AdviceStep("💊", "কপার হাইড্রোক্সাইড স্প্রে করুন"),
                    AdviceStep("🔁", "প্রতি ১০ দিনে একবার স্প্রে করুন"),
                    AdviceStep("🗑️", "রোগাক্রান্ত অংশ মাটিতে পুঁতে ফেলুন")
                )
            ),
            "Cutting Weevil" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("✂️", "আক্রান্ত ডালপালা কেটে ফেলুন"),
                    AdviceStep("💊", "ক্লোরপাইরিফস কীটনাশক স্প্রে করুন"),
                    AdviceStep("👋", "পোকা হাতে বেছে মেরে ফেলুন"),
                    AdviceStep("🧹", "গাছের চারপাশ পরিষ্কার রাখুন"),
                    AdviceStep("👁️", "প্রতি সপ্তাহে গাছ পরীক্ষা করুন")
                )
            ),
            "Die Back" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("✂️", "মরা ও শুকনো ডাল কেটে ফেলুন"),
                    AdviceStep("🟤", "কাটা জায়গায় বোর্দো পেস্ট লাগান"),
                    AdviceStep("💊", "কার্বেন্ডাজিম স্প্রে করুন"),
                    AdviceStep("🌱", "গাছে পর্যাপ্ত সার ও পানি দিন"),
                    AdviceStep("👁️", "প্রতি ১৫ দিনে গাছ পরীক্ষা করুন")
                )
            ),
            "Gall Midge" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("🔥", "আক্রান্ত কুঁড়ি ও পাতা তুলে পুড়িয়ে দিন"),
                    AdviceStep("💊", "ইমিডাক্লোপ্রিড কীটনাশক স্প্রে করুন"),
                    AdviceStep("📅", "ফুল আসার আগেই স্প্রে দিন"),
                    AdviceStep("🧹", "বাগান সবসময় পরিষ্কার রাখুন"),
                    AdviceStep("🔁", "প্রতি ১০ দিনে একবার স্প্রে করুন")
                )
            ),
            "Healthy" to DiseaseAdvice(
                headerTitle = "✅ সুসংবাদ! গাছ সুস্থ আছে",
                isHealthy = true,
                steps = listOf(
                    AdviceStep("😊", "গাছ ভালো আছে, চিন্তার কিছু নেই!"),
                    AdviceStep("💧", "নিয়মিত সঠিক পরিমাণে পানি দিন"),
                    AdviceStep("🌱", "সময়মতো সার দিন"),
                    AdviceStep("🧹", "গাছের চারপাশ পরিষ্কার রাখুন"),
                    AdviceStep("👁️", "প্রতি সপ্তাহে গাছ পরীক্ষা করুন")
                )
            ),
            "Powdery Mildew" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("🍃", "আক্রান্ত পাতা ও ফুল সরিয়ে ফেলুন"),
                    AdviceStep("💊", "সালফার বা হেক্সাকোনাজল স্প্রে করুন"),
                    AdviceStep("🔁", "প্রতি ১০ দিনে একবার স্প্রে দিন"),
                    AdviceStep("💧", "গাছে অতিরিক্ত পানি দেবেন না"),
                    AdviceStep("🌬️", "গাছের পাশে বায়ু চলাচল ভালো রাখুন")
                )
            ),
            "Sooty Mould" to DiseaseAdvice(
                headerTitle = "⚠️ করণীয় পদক্ষেপ",
                isHealthy = false,
                steps = listOf(
                    AdviceStep("🧴", "সাবান-পানি মিশিয়ে পাতা মুছে দিন"),
                    AdviceStep("💊", "কীটনাশক স্প্রে করে পোকামাকড় দূর করুন"),
                    AdviceStep("✂️", "আক্রান্ত পাতা কেটে সরিয়ে দিন"),
                    AdviceStep("☀️", "গাছে পর্যাপ্ত আলো ও বায়ু নিশ্চিত করুন"),
                    AdviceStep("👁️", "প্রতি ১৫ দিনে গাছ পরীক্ষা করুন")
                )
            )
        )
    }
}
