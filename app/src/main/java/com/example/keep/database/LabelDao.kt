package com.example.keep.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)

    @Query("select * from label_table order by labelId DESC limit 1")
    fun getLast() : Label

    @Update
    fun update(label: Label)

    @Delete
    suspend fun delete(label: Label)

    @Query("select * from label_table order by labelId ASC")
    fun getAllLabels() : LiveData<MutableList<Label>>

    @Query("select * from label_table where labelName Match :query")
    suspend fun search(query: String) : List<Label>

    @Transaction
    @Query("SELECT * FROM label_table order by labelId ASC")
    fun getLabelsWithNotes(): LiveData<List<LabelWithNotes>>

    @Transaction
    @Query("SELECT * FROM label_table where labelId = :labelId")
    fun getNotesOfLabel(labelId : Int): LabelWithNotes

}