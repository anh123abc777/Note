package com.example.keep

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.keep.database.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class NoteDatabaseTest {

    private lateinit var db : NoteDatabase
    private lateinit var noteRepository: NoteRepository
    private lateinit var labelRepository: LabelRepository


    @Before
    fun createDb(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        noteRepository = NoteRepository(db.noteDao)
        labelRepository = LabelRepository(db.labelDao)
    }


    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    fun insertLabel(){
        runBlocking {
            labelRepository.insert(Label("Inspiration"))
        }
        val last = labelRepository.getLastLabel()
        val s = labelRepository.get(last.labelId)
        assertEquals("Inspiration",last)
    }
//
    @Test
    @Throws(Exception::class)
    fun insertAndGetNote(){
        val note = Note()
        runBlocking {
            noteRepository.insert(note)
        }
        val lastNote = noteRepository.getLastNote()
        val lastNoteWithLabels = noteRepository.get(lastNote.noteId)
        assertEquals(NoteWithLabels(lastNote, emptyList()),lastNoteWithLabels)
    }

//    @Test
//    fun addLabelToNote(){
//        runBlocking {
//            labelRepository.insert(Label("Inspiration"))
//        }
//        val label = labelRepository.getLastLabel()
//        runBlocking {
//            labelRepository.insert(Label("Healthy"))
//        }
//        val label1 = labelRepository.getLastLabel()
//
//        runBlocking {
//            noteRepository.insert(Note())
//        }
//        val noteWithLabels = noteRepository.getLastNote()
//
//        runBlocking {
//            noteRepository.addLabelToNote(noteWithLabels,label)
//            noteRepository.addLabelToNote(noteWithLabels,label1)
//        }
//
//        val noteWithLabels = noteRepository.getLabelsOfNote(noteWithLabels)
//        assertEquals(listOf(label,label1),noteWithLabels)
//    }


//    @Test
//    fun AllNoteViewModelAndDatabase(){
//        runBlocking {
//            noteRepository.insert(Note(title = "2"))
//            noteRepository.insert(Note(title = "3"))
//            noteRepository.insert(Note(title = "4"))
//            noteRepository.insert(Note(title = "5"))
//            noteRepository.insert(Note(title = "6"))
//
//        }
//        runBlocking {
//            labelRepository.insert(Label("Healthy"))
//        }
//        val label1 = labelRepository.getLastLabel()
//        val noteWithLabels = noteRepository.getLastNote()
//
//        runBlocking {
//            noteRepository.addLabelToNote(noteWithLabels,label1)
//        }
//
//        assertEquals(noteWithLabels,noteRepository.getNotesWithLabels())
//
//    }

//    @Test
//    fun getNoteWithLabels(){
//        runBlocking {
//            noteRepository.insert(Note(title = "2"))
//            noteRepository.insert(Note(title = "3"))
//            noteRepository.insert(Note(title = "4"))
//            noteRepository.insert(Note(title = "5"))
//            noteRepository.insert(Note(title = "6"))
//        }
//        runBlocking {
//            labelRepository.insert(Label("Inspiration"))
//        }
//        val label = labelRepository.getLastLabel()
//        runBlocking {
//            labelRepository.insert(Label("Healthy"))
//        }
//        val label1 = labelRepository.getLastLabel()
//        val noteWithLabels = noteRepository.getLastNote()
//
//        runBlocking {
//            noteRepository.addLabelToNote(noteWithLabels,label)
//            noteRepository.addLabelToNote(noteWithLabels,label1)
//        }
//
//        val noteWithLabels = noteRepository.get(noteWithLabels.noteId)
//        assertEquals("",noteWithLabels)
//    }

}