package com.example.phone.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tse_emotionalrecognition.common.data.database.UserRepository
import com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats
import kotlinx.coroutines.launch

class InterventionTrackerViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val NOTIFICATION_ID = 1L

    private val _interventionStats = MutableLiveData<InterventionStats>()
    val interventionStats: MutableLiveData<InterventionStats> = _interventionStats

    init {
        loadInterventionStats()
    }

    private fun loadInterventionStats() {
        viewModelScope.launch {
            val stats = userRepository.getInterventionStatsById(NOTIFICATION_ID)
            _interventionStats.postValue(stats)
        }
    }

    fun incrementTriggered() {
        viewModelScope.launch {
            val stats = userRepository.getInterventionStatsById(NOTIFICATION_ID)
            if (stats != null) {
                userRepository.incrementTriggered(viewModelScope, NOTIFICATION_ID) {
                    _interventionStats.postValue(it)
                }
            }
        }
    }

    fun incrementDismissed() {
        viewModelScope.launch {
            val stats = userRepository.getInterventionStatsById(NOTIFICATION_ID)
            if (stats != null) {
                userRepository.incrementDismissed(viewModelScope, NOTIFICATION_ID) {
                    _interventionStats.postValue(it)
                }
            }
        }

    }
}

class InterventionTrackerViewModelFactory(private val userRepository: UserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterventionTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InterventionTrackerViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
