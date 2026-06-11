package com.example.mango_disease_detector.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mango_disease_detector.model.PredictionResponse
import com.example.mango_disease_detector.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val result: PredictionResponse) : UiState()
    data class Error(val message: String) : UiState()
}

class DetectorViewModel : ViewModel() {
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun predict(imageFile: File) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { ApiService.predict(imageFile) }
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
