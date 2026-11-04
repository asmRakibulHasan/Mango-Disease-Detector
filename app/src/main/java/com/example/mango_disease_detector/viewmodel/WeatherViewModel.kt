package com.example.mango_disease_detector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mango_disease_detector.data.CachedWeather
import com.example.mango_disease_detector.data.SavedPlace
import com.example.mango_disease_detector.data.WeatherPrefs
import com.example.mango_disease_detector.model.WeatherResponse
import com.example.mango_disease_detector.network.WeatherService
import com.example.mango_disease_detector.utils.FarmAdvice
import com.example.mango_disease_detector.utils.FarmAdvisor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class WeatherUiState {
    data class Loading(val findingLocation: Boolean = false) : WeatherUiState()
    data class Success(
        val weather: WeatherResponse,
        val placeName: String,
        val updatedAt: Long,
        val fromCache: Boolean,
        val advice: List<FarmAdvice>
    ) : WeatherUiState()
    object Error : WeatherUiState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableLiveData<WeatherUiState>()
    val state: LiveData<WeatherUiState> = _state

    private var loadJob: Job? = null

    fun showFindingLocation() {
        _state.value = WeatherUiState.Loading(findingLocation = true)
    }

    fun load(place: SavedPlace) {
        loadJob?.cancel()
        _state.value = WeatherUiState.Loading()
        loadJob = viewModelScope.launch {
            val app = getApplication<Application>()
            try {
                val weather = withContext(Dispatchers.IO) { WeatherService.forecast(place.lat, place.lon) }
                val now = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    WeatherPrefs.saveCache(app, CachedWeather(weather, place.name, now))
                }
                _state.value = WeatherUiState.Success(
                    weather, place.name, now, fromCache = false, advice = FarmAdvisor.advise(weather)
                )
            } catch (e: Exception) {
                val cached = withContext(Dispatchers.IO) { WeatherPrefs.loadCache(app) }
                _state.value = if (cached != null) {
                    WeatherUiState.Success(
                        cached.response, cached.placeName, cached.savedAt,
                        fromCache = true, advice = FarmAdvisor.advise(cached.response)
                    )
                } else {
                    WeatherUiState.Error
                }
            }
        }
    }
}
