package com.example.keep.labelsetting

import android.app.Application

import androidx.lifecycle.ViewModel
import com.example.keep.database.Label
import com.example.keep.database.LabelRepository
import com.example.keep.database.NoteDatabase
import kotlinx.coroutines.*

class LabelSettingViewModel(application: Application): ViewModel() {

    private val labelRepository =LabelRepository(NoteDatabase.getInstance(application).labelDao)
    val allLabels =   labelRepository.allLabels

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

    fun removeLabel(labelId: Int){
        allLabels.value!!.removeIf { it.labelId==labelId }
    }

    fun saveData(){
        allLabels.value!!.forEach {
            coroutineScope.launch(Dispatchers.IO){
                labelRepository.insert(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}