package com.example.keep.labelsetting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LabelSettingViewModelFactory(
    private val application: Application): ViewModelProvider.Factory {

    @Suppress("Unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LabelSettingViewModel::class.java))
            return LabelSettingViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}