package com.example.keep.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LabelWithNotes(
    @Embedded val label: Label,
    @Relation(
        parentColumn = "labelId",
        entityColumn = "noteId",
        associateBy = Junction(NoteLabelCrossRef::class)
    )
    val notes : List<Note>
)