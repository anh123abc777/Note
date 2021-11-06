package com.example.keep.database
//
//import androidx.room.Embedded
//import androidx.room.Relation
//
//data class NoteWithCheckboxes (
//    @Embedded val note: Note,
//    @Relation(
//        parentColumn = "noteId",
//        entityColumn = "onwerId"
//    )
//    val checkboxes: MutableList<Checkbox>
//
//    )