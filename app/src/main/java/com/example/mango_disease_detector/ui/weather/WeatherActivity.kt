package com.example.mango_disease_detector.ui.weather

import android.Manifest
import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.data.Districts
import com.example.mango_disease_detector.data.SavedPlace
import com.example.mango_disease_detector.data.WeatherPrefs
import com.example.mango_disease_detector.databinding.ActivityWeatherBinding
import com.example.mango_disease_detector.databinding.ItemAdviceBinding
import com.example.mango_disease_detector.databinding.ItemForecastDayBinding
import com.example.mango_disease_detector.utils.BanglaFormat
import com.example.mango_disease_detector.utils.FarmAdvice
import com.example.mango_disease_detector.utils.FarmAdvisor
import com.example.mango_disease_detector.utils.LocationHelper
import com.example.mango_disease_detector.utils.edgeToEdge
import com.example.mango_disease_detector.viewmodel.WeatherUiState
import com.example.mango_disease_detector.viewmodel.WeatherViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var lastSuccess: WeatherUiState.Success? = null
    private var locationJob: Job? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            useGpsLocation(fallback = WeatherPrefs.loadPlace(this))
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            showDistrictPicker()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tts = TextToSpeech(this, this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnChangeLocation.setOnClickListener { showDistrictPicker() }
        binding.btnRetry.setOnClickListener { startLoad() }
        binding.btnSpeak.setOnClickListener { speakWeather() }
        binding.swipeRefresh.setColorSchemeResources(R.color.green_primary)
        binding.swipeRefresh.setOnRefreshListener { startLoad() }

        WeatherPrefs.loadPlace(this)?.let { binding.tvLocation.text = it.name }

        viewModel.state.observe(this) { render(it) }

        if (viewModel.state.value == null) startLoad()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("bn", "BD"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    // ---------- Location ----------

    private fun startLoad() {
        val saved = WeatherPrefs.loadPlace(this)
        when {
            saved == null && LocationHelper.hasPermission(this) -> useGpsLocation(fallback = null)
            saved == null -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            saved.isGps && LocationHelper.hasPermission(this) -> useGpsLocation(fallback = saved)
            else -> viewModel.load(saved)
        }
    }

    private fun useGpsLocation(fallback: SavedPlace?) {
        viewModel.showFindingLocation()
        locationJob?.cancel()
        locationJob = lifecycleScope.launch {
            val location = LocationHelper.getLocation(this@WeatherActivity)
            when {
                location != null -> {
                    val name = Districts.placeName(location.latitude, location.longitude)
                        ?: getString(R.string.weather_my_location_name)
                    val place = SavedPlace(name, location.latitude, location.longitude, isGps = true)
                    WeatherPrefs.savePlace(this@WeatherActivity, place)
                    viewModel.load(place)
                }
                fallback != null -> viewModel.load(fallback)
                else -> {
                    Toast.makeText(
                        this@WeatherActivity, getString(R.string.weather_location_not_found), Toast.LENGTH_LONG
                    ).show()
                    showDistrictPicker()
                }
            }
        }
    }

    private fun showDistrictPicker() {
        val hasPlace = WeatherPrefs.loadPlace(this) != null
        val items = arrayOf(getString(R.string.weather_my_location)) +
                Districts.all.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.weather_choose_location)
            .setItems(items) { _, which ->
                if (which == 0) {
                    if (LocationHelper.hasPermission(this)) {
                        useGpsLocation(fallback = WeatherPrefs.loadPlace(this))
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                } else {
                    val d = Districts.all[which - 1]
                    val place = SavedPlace(d.name, d.lat, d.lon, isGps = false)
                    WeatherPrefs.savePlace(this, place)
                    viewModel.load(place)
                }
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ -> if (!hasPlace) finish() }
            .setCancelable(hasPlace)
            .show()
    }

    // ---------- Rendering ----------

    private fun render(state: WeatherUiState) {
        when (state) {
            is WeatherUiState.Loading -> {
                // Pull-to-refresh shows its own spinner; keep old content visible meanwhile.
                if (binding.swipeRefresh.isRefreshing && lastSuccess != null) return
                binding.tvLoading.setText(
                    if (state.findingLocation) R.string.weather_finding_location else R.string.weather_loading
                )
                binding.layoutLoading.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            }
            is WeatherUiState.Error -> {
                binding.swipeRefresh.isRefreshing = false
                binding.layoutLoading.visibility = View.GONE
                binding.layoutContent.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            }
            is WeatherUiState.Success -> {
                binding.swipeRefresh.isRefreshing = false
                showWeather(state)
            }
        }
    }

    private fun showWeather(s: WeatherUiState.Success) {
        val firstShow = binding.layoutContent.visibility != View.VISIBLE
        lastSuccess = s
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE

        val w = s.weather
        val current = w.current ?: return
        val daily = w.daily
        val today = FarmAdvisor.todayIndex(w)
        val next = FarmAdvisor.next24h(w)

        binding.tvLocation.text = s.placeName
        binding.tvOffline.visibility = if (s.fromCache) View.VISIBLE else View.GONE

        binding.tvTemperature.text = BanglaFormat.temp(current.temperature)
        binding.tvCondition.text = BanglaFormat.weatherText(current.weatherCode)
        binding.ivWeatherIcon.setImageResource(weatherIconRes(current.weatherCode, current.isDay == 1))

        val details = mutableListOf(getString(R.string.weather_feels_like, BanglaFormat.temp(current.feelsLike)))
        val max = daily?.tempMax?.getOrNull(today)
        val min = daily?.tempMin?.getOrNull(today)
        if (max != null && min != null) {
            details += getString(R.string.weather_max_min, BanglaFormat.temp(max), BanglaFormat.temp(min))
        }
        binding.tvDetails.text = details.joinToString(" · ")

        binding.tvHumidity.text = BanglaFormat.number(current.humidity) + "%"
        binding.tvRainChance.text = BanglaFormat.number(next.maxRainChance) + "%"
        binding.tvWind.text = getString(R.string.weather_wind_unit, BanglaFormat.number(current.windSpeed))
        binding.tvUpdated.text = getString(R.string.weather_updated, BanglaFormat.updatedAt(s.updatedAt))

        showAdvice(s.advice)
        showForecast(s)

        if (firstShow) {
            binding.layoutContent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        }
    }

    private fun showAdvice(advice: List<FarmAdvice>) {
        binding.layoutAdvice.removeAllViews()
        advice.forEach { item ->
            val row = ItemAdviceBinding.inflate(layoutInflater, binding.layoutAdvice, false)
            val style = AdviceStyle.of(item.level)
            row.root.backgroundTintList = ColorStateList.valueOf(getColor(style.bg))
            row.adviceTile.backgroundTintList = ColorStateList.valueOf(getColor(style.icon))
            row.ivAdviceIcon.setImageResource(style.drawable)
            row.tvAdviceLabel.setText(style.label)
            row.tvAdviceLabel.setTextColor(getColor(style.labelColor))
            row.tvAdviceTitle.text = item.title
            row.tvAdviceTitle.setTextColor(getColor(style.title))
            row.tvAdviceText.text = item.text
            row.tvAdviceText.setTextColor(getColor(style.body))
            binding.layoutAdvice.addView(row.root)
        }
    }

    private fun showForecast(s: WeatherUiState.Success) {
        binding.layoutForecast.removeAllViews()
        val daily = s.weather.daily ?: return
        val today = FarmAdvisor.todayIndex(s.weather)
        for (i in today until daily.time.size) {
            val offset = i - today
            val item = ItemForecastDayBinding.inflate(layoutInflater, binding.layoutForecast, false)
            item.tvDay.text = BanglaFormat.dayLabel(daily.time[i], offset)
            item.ivDayIcon.setImageResource(weatherIconRes(daily.weatherCode.getOrNull(i) ?: 3))
            item.tvDayMax.text = daily.tempMax.getOrNull(i)?.let { BanglaFormat.temp(it) } ?: "-"
            item.tvDayMin.text = daily.tempMin.getOrNull(i)?.let { BanglaFormat.temp(it) } ?: "-"
            item.tvDayRain.text = daily.rainChance.getOrNull(i)?.let { BanglaFormat.number(it) + "%" } ?: "-"
            if (offset == 0) item.root.setBackgroundResource(R.drawable.bg_day_today)
            binding.layoutForecast.addView(item.root)
        }
    }

    // ---------- Voice ----------

    private fun speakWeather() {
        val s = lastSuccess ?: return
        if (!ttsReady) {
            Toast.makeText(this, getString(R.string.weather_voice_not_ready), Toast.LENGTH_SHORT).show()
            return
        }
        val current = s.weather.current ?: return
        val next = FarmAdvisor.next24h(s.weather)
        val text = buildString {
            append("${s.placeName}। ")
            append("আজকের আবহাওয়া ${BanglaFormat.weatherText(current.weatherCode)}। ")
            append("তাপমাত্রা ${BanglaFormat.number(current.temperature)} ডিগ্রি। ")
            append("বাতাসের আর্দ্রতা ${BanglaFormat.number(current.humidity)} শতাংশ। ")
            append("বৃষ্টির সম্ভাবনা ${BanglaFormat.number(next.maxRainChance)} শতাংশ। ")
            append("পরামর্শ। ")
            s.advice.forEach { append(it.title).append("। ").append(it.text).append(" ") }
        }
        val spoken = text.replace("%", " শতাংশ").replace("°", " ডিগ্রি").replace("(", "। ").replace(")", "")
        tts?.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "weather")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
