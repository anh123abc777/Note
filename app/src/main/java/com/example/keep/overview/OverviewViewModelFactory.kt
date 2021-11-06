package com.example.keep.overview

import android.app.Activity
import android.app.Application
import android.view.Window
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OverviewViewModelFactory(private val activity: Activity,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(OverviewViewModel::class.java))
            return OverviewViewModel(activity,application) as T
        throw IllegalArgumentException("Unknown viewmodel class")
    }
}