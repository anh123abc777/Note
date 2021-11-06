package com.example.keep.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithLabels(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "labelId",
        associateBy = Junction(NoteLabelCrossRef::class)
    )
    val labels : List<Label>?= emptyList()
)