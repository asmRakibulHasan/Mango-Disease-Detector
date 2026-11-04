package com.example.mango_disease_detector.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.example.mango_disease_detector.utils.BanglaFormat
import com.example.mango_disease_detector.utils.PermissionUtils
import java.io.File

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
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
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

        binding.btnWeather.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onResume() {
        super.onResume()
        val cached = WeatherPrefs.loadCache(this)?.response?.current
        if (cached != null) {
            binding.tvHomeWeatherTemp.text = "· " + BanglaFormat.weatherEmoji(cached.weatherCode, cached.isDay == 1) +
                    " " + BanglaFormat.temp(cached.temperature)
            binding.tvHomeWeatherTemp.visibility = View.VISIBLE
        } else {
            binding.tvHomeWeatherTemp.visibility = View.GONE
        }
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
