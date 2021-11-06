package com.example.keep.label

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LabelViewModelFactory(
    private val notesIdToLabel: IntArray,
    private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabelViewModel::class.java))
            return LabelViewModel(notesIdToLabel,application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}