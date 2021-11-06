package com.example.keep.database

import androidx.room.Entity


@Entity(primaryKeys = ["noteId","labelId"], tableName = "note_label_cross_ref")
data class NoteLabelCrossRef(
    val noteId: Int,
    val labelId: Int
)
