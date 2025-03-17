package com.example.phone.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement
import kotlinx.coroutines.launch

class HealthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _heartRateList = MutableLiveData<List<HeartRateMeasurement>>()
    val heartRateList: LiveData<List<HeartRateMeasurement>> = _heartRateList

    private val _skinTemperatureList = MutableLiveData<List<SkinTemperatureMeasurement>>()
    val skinTemperatureList: LiveData<List<SkinTemperatureMeasurement>> = _skinTemperatureList

    init {
        loadHealthData()
    }

    private fun loadHealthData() {
        viewModelScope.launch {
            _heartRateList.value = userRepository.getHeartRateMeasurements()
            _skinTemperatureList.value = userRepository.getSkinTemperatureMeasurements()
        }
    }
}

class HealthDataViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
