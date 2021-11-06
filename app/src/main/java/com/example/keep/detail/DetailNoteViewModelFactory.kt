package com.example.keep.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keep.database.NoteRepository
import com.example.keep.database.NoteWithLabels
import java.lang.IllegalArgumentException

class DetailNoteViewModelFactory(
    private val note : NoteWithLabels,
    private val repository: NoteRepository): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DetailNoteViewModel::class.java))
            return DetailNoteViewModel(note,repository)  as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}