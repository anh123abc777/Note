package com.example.keep.database

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelRepository @Inject constructor(private val labelDao: LabelDao) {

    val allLabelWithNotes : LiveData<List<LabelWithNotes>> = labelDao.getLabelsWithNotes()

    val allLabels : LiveData<MutableList<Label>> = labelDao.getAllLabels()

    suspend fun insert(label: Label){
        labelDao.insert(label)
    }

    suspend fun remove(label: Label){
        labelDao.delete(label)
    }

    fun update(label: Label){
        labelDao.update(label)
    }

    fun get(labelId: Int): LabelWithNotes = labelDao.getNotesOfLabel(labelId)

    suspend fun search(query : String) : List<Label> = labelDao.search(query)

    fun getLastLabel() : Label = labelDao.getLast()

}