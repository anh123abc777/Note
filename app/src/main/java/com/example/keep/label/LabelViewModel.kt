package com.example.keep.label

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.keep.TriStateMaterialCheckBox
import com.example.keep.adapter.DataLabelsAdapter
import com.example.keep.database.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception

class LabelViewModel(private val noteIdsToLabel: IntArray?,
                     val application: Application) : ViewModel() {


    var notesToLabel : List<NoteWithLabels>
    private val noteRepository = NoteRepository(NoteDatabase.getInstance(application).noteDao)
    private val labelRepository = LabelRepository(NoteDatabase.getInstance(application).labelDao)
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var labels: LiveData<List<LabelWithNotes>> = labelRepository.allLabelWithNotes
    var noteLabelCrossRef: LiveData<List<NoteLabelCrossRef>> = noteRepository.allNoteLabelCrossRef

    init {
        notesToLabel = noteIdsToLabel?.map {
            runBlocking {
                withContext(Dispatchers.IO) {
                    noteRepository.get(it)
                }
            }
        } ?: listOf(NoteWithLabels(Note(), emptyList()))

//        runBlocking {
//            withContext(Dispatchers.IO){
//                labelRepository.insert(Label("Inspiration"))
//                labelRepository.insert(Label("Healthy"))
//                labelRepository.insert(Label("Homework"))
//                labelRepository.insert(Label("Timetable"))
//                labelRepository.insert(Label("Future"))
//            }
//        }
    }

    fun updateNotesToLabel(){
        notesToLabel = noteIdsToLabel?.map {
            runBlocking {
                withContext(Dispatchers.IO) {
                    noteRepository.get(it)
                }
            }
        } ?: emptyList()
    }


    var dataAdapter = mutableListOf<DataLabelsAdapter>()

    fun createDataAdapter() : MutableList<DataLabelsAdapter>{

        dataAdapter.clear()

        labels.value!!.forEach { labelWithNotes ->

            when {
                notesToLabel.all {  noteWithLabels ->
                    noteWithLabels?.labels?.contains(labelWithNotes.label) == true
                            } -> {
                                dataAdapter.add(DataLabelsAdapter(labelWithNotes.label, TriStateMaterialCheckBox.STATE_CHECKED))
                            }

                notesToLabel.all {  noteWithLabels ->
                    noteWithLabels?.labels?.contains(labelWithNotes.label) == false
                            } -> {
                                dataAdapter.add(DataLabelsAdapter(labelWithNotes.label,TriStateMaterialCheckBox.STATE_UNCHECKED))
                            }

                notesToLabel.all {  noteWithLabels ->
                    noteWithLabels== null
                } -> {
                    dataAdapter.add(DataLabelsAdapter(labelWithNotes.label,TriStateMaterialCheckBox.STATE_UNCHECKED))

                }

                else -> {
                              dataAdapter.add(DataLabelsAdapter(labelWithNotes.label,TriStateMaterialCheckBox.STATE_INDETERMINATE))
                }
            }
        }

        return dataAdapter
    }

    var stateSaveDb = false

    fun isNewDb(): Boolean{
        stateSaveDb = true
        return stateSaveDb
    }

    fun doneUpdateDb(){
        stateSaveDb = false
    }

    fun addLabelToNote(label: Label) {
        try {
            notesToLabel?.forEach { noteWithLabels ->
                runBlocking {
                    withContext(Dispatchers.IO) {
                        noteRepository.addLabelToNote(noteWithLabels.note, label)
                    }
                }
            }
        }
        catch (e : Exception) {
            Timber.i("done add Label To Note")
        }
    }

    fun removeAllLabelOnNotes(label: Label){
        notesToLabel.forEach { noteWithLabels ->
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        noteRepository.removeLabelOnNote(noteWithLabels.note, label)
                    }catch (e: NullPointerException){
                    }
                }
            }
        }
        Timber.i("done delete Label On Note")
    }


}