package com.example.keep.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(notes : List<Note>)

    @Query("select * from note_table where noteId = :key")
    fun get(key : Int) : Note

    @Query("select MAX(position) from note_table")
    fun getLastPosition() : Int

    @Query("select MIN(position) from note_table")
    fun getFirstPosition() : Int

    @Delete
    suspend fun delete(note: Note)

    @Query("select * from note_table where state = 0 order by position ASC")
    fun getAllNotes() : LiveData<List<Note>>

    @Query("select * from note_table where state = 0 order by position ASC")
    fun getAllNormalNotes() : LiveData<List<NoteWithLabels>>


    @Query("select * from note_table where state = 1 order by position ASC")
    fun getAllArchiveNotes() : LiveData<List<NoteWithLabels>>

    @Query("select * from note_table where state = -1 order by position ASC")
    fun getAllTrashNotes() : LiveData<List<NoteWithLabels>>

    @Query("select * from note_table order by noteId DESC limit 1")
    fun getLastNote() : Note

    @Transaction
        @Query("SELECT * FROM note_table where state = 0 order by position ASC")
    fun getNotesWithLabels():  LiveData<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM note_table where noteId = :noteId")
    fun getLabelsOfNote(noteId : Int): NoteWithLabels

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteLabelAcrossRef(crossRef: NoteLabelCrossRef)

    @Delete
    suspend fun deleteNoteLabelAcrossRef(crossRef: NoteLabelCrossRef)

    @Query("select * from note_label_cross_ref")
    fun getAllNoteLabelCrossRef() : LiveData<List<NoteLabelCrossRef>>

//    @Transaction
//    @Query("SELECT * FROM note_table")
//    fun getUsersWithPlaylists(): List<NoteWithCheckboxes>
//


}