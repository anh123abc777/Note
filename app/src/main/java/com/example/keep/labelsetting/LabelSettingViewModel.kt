package com.example.keep.labelsetting

import android.app.Application

import androidx.lifecycle.ViewModel
import com.example.keep.database.Label
import com.example.keep.database.LabelRepository
import com.example.keep.database.NoteDatabase
import kotlinx.coroutines.*
import timber.log.Timber

class LabelSettingViewModel(application: Application): ViewModel() {

    private val labelRepository =LabelRepository(NoteDatabase.getInstance(application).labelDao)
    val allLabels =   labelRepository.allLabels

    private var labelsWillBeRemove = mutableListOf<Label>()

    private val job = Job()
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)


    private var lastId =
        runBlocking {
            try {
                withContext(Dispatchers.IO) {
                    labelRepository.getLastLabel().labelId
                }
            }catch (e : NullPointerException){
                0
            }
    }




    fun addLabel(labelName : String) {
        lastId++
        allLabels.value!!.add(Label(lastId,labelName))
    }

    fun removeLabel(label: Label){
        val labelWillBeRemove = allLabels.value?.find { it==label }

        labelWillBeRemove?.let { labelsWillBeRemove.add(it) }

        allLabels.value?.remove(label)


//        runBlocking(Dispatchers.IO){
//            labelWillBeRemove?.let { labelRepository.remove(it) }
//        }

    }

    fun updateLabel(label: Label){
        val index = allLabels.value!!.indexOfFirst { it.labelId == label.labelId }
        allLabels.value?.get(index)?.labelName = label.labelName
        Timber.i("${allLabels.value?.find { it.labelId==label.labelId }?.labelName}")
    }

    fun saveData(){
        allLabels.value!!.forEach {
            runBlocking(Dispatchers.IO){
                    labelRepository.insert(it)
            }
        }

        labelsWillBeRemove.forEach {
            runBlocking(Dispatchers.IO){
                labelRepository.remove(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}