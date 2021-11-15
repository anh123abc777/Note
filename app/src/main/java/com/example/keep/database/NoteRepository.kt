package com.example.keep.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor (private val noteDao : NoteDao){

    var allNotes : LiveData<List<NoteWithLabels>> = noteDao.getNotesWithLabels()
    var allNotesTrash : LiveData<List<NoteWithLabels>> = noteDao.getAllTrashNotes()
    var allNotesArchive : LiveData<List<NoteWithLabels>> = noteDao.getAllArchiveNotes()

    var allNoteLabelCrossRef : LiveData<List<NoteLabelCrossRef>> = noteDao.getAllNoteLabelCrossRef()


     fun insert(note : Note){
        noteDao.insert(note)
    }

    suspend fun delete(note: Note){
        noteDao.delete(note)
    }

    fun update(note: Note){
        noteDao.update(note)
    }

    fun updateAll(notes : List<Note>){
        noteDao.updateAll(notes)
    }

    fun getLastPosition() : Int = noteDao.getLastPosition()

    fun getFirstPosition(): Int = noteDao.getFirstPosition()

    fun getRawNote(noteId: Int) : Note = noteDao.get(noteId)

    fun get(noteId : Int) : NoteWithLabels = noteDao.getLabelsOfNote(noteId)

//    fun getNormalNotes(listNoteId : List<Int>): List<LiveData<NoteWithLabels>> {
//        var list = listOf<LiveData<NoteWithLabels>>()
//        list = listNoteId.map {
//             noteDao.getLabelsOfNote(it)
//        }
//        return list
//    }

    fun getLastNote() : Note = noteDao.getLastNote()

    fun getNotesWithLabels() : LiveData<List<NoteWithLabels>> = noteDao.getNotesWithLabels()

    suspend fun addLabelToNote(note: Note, label: Label){
        noteDao.insertNoteLabelAcrossRef(NoteLabelCrossRef(note.noteId,label.labelId))
    }

    suspend fun removeLabelOnNote(note: Note,label: Label){
        noteDao.deleteNoteLabelAcrossRef(NoteLabelCrossRef(note.noteId,label.labelId))
    }

}