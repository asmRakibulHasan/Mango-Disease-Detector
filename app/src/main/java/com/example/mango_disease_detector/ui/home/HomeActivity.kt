package com.example.mango_disease_detector.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.data.WeatherPrefs
import com.example.mango_disease_detector.databinding.ActivityHomeBinding
import com.example.mango_disease_detector.ui.result.ResultActivity
import com.example.mango_disease_detector.ui.weather.WeatherActivity
import com.example.mango_disease_detector.ui.weather.weatherIconRes
import com.example.mango_disease_detector.utils.AdviceLevel
import com.example.mango_disease_detector.utils.BanglaFormat
import com.example.mango_disease_detector.utils.FarmAdvisor
import com.example.mango_disease_detector.utils.edgeToEdge
import com.example.mango_disease_detector.utils.PermissionUtils
import java.io.File
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var cameraImageUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) launchCamera()
        else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) launchGallery()
        else Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri?.let { openResult(it) }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { openResult(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge(lightIcons = true)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Green header runs under the status bar; only its content is pushed down.
        val headerTop = binding.headerContent.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            binding.headerContent.setPadding(
                binding.headerContent.paddingLeft, headerTop + bars.top,
                binding.headerContent.paddingRight, binding.headerContent.paddingBottom
            )
            insets
        }

        binding.btnCamera.setOnClickListener {
            if (PermissionUtils.hasCameraPermission(this)) launchCamera()
            else cameraPermissionLauncher.launch(PermissionUtils.cameraPermissions())
        }

        binding.btnGallery.setOnClickListener {
            if (PermissionUtils.hasStoragePermission(this)) launchGallery()
            else storagePermissionLauncher.launch(PermissionUtils.storagePermissions())
        }

        binding.btnWeather.setOnClickListener { openWeather() }
        binding.chipWeather.setOnClickListener { openWeather() }
    }

    override fun onResume() {
        super.onResume()
        binding.tvGreeting.text = getString(
            R.string.home_greeting, BanglaFormat.greeting(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        )

        val place = WeatherPrefs.loadPlace(this)
        binding.layoutPlace.visibility = if (place != null) View.VISIBLE else View.INVISIBLE
        binding.tvPlace.text = place?.name.orEmpty()

        val cached = WeatherPrefs.loadCache(this)
        val current = cached?.response?.current
        if (cached == null || current == null) {
            binding.chipWeather.visibility = View.GONE
            binding.ivHomeWeather.setImageResource(R.drawable.wx_partly)
            binding.tvHomeWeather.setText(R.string.home_weather_tap)
            binding.tvHomeAdvice.visibility = View.GONE
            return
        }

        val temp = BanglaFormat.temp(current.temperature)
        binding.chipWeather.visibility = View.VISIBLE
        binding.tvChipTemp.text = temp
        val icon = weatherIconRes(current.weatherCode, current.isDay == 1)
        binding.ivChipWeather.setImageResource(icon)
        binding.ivHomeWeather.setImageResource(icon)
        binding.tvHomeWeather.text = "$temp · ${BanglaFormat.weatherText(current.weatherCode)}"

        val urgent = FarmAdvisor.advise(cached.response)
            .count { it.level == AdviceLevel.DANGER || it.level == AdviceLevel.CAUTION }
        binding.tvHomeAdvice.visibility = View.VISIBLE
        binding.tvHomeAdvice.text = if (urgent > 0) {
            getString(R.string.home_urgent_advice, BanglaFormat.digits(urgent.toString()))
        } else {
            getString(R.string.home_no_urgent_advice)
        }
    }

    private fun openWeather() {
        startActivity(Intent(this, WeatherActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun launchCamera() {
        val file = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        cameraLauncher.launch(cameraImageUri!!)
    }

    private fun launchGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openResult(uri: Uri) {
        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
