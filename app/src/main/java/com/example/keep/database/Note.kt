package com.example.keep.database

import android.graphics.Color
import androidx.room.*
import com.example.keep.checkbox.CheckboxGroupAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var noteId : Int = 0,

    @ColumnInfo(name = "title")
    var title : String = "",

    @ColumnInfo(name = "content")
    var content : String = "",

    @ColumnInfo(name = "images")
    var images : ArrayList<String> = arrayListOf(),

    @ColumnInfo(name = "checkboxes")
    var checkboxes : ArrayList<DataCheckboxes> = arrayListOf(),

    @ColumnInfo(name = "background")
    var background : Int = Color.WHITE,

    @ColumnInfo(name = "type")
    var priority : Int = 0,

    @ColumnInfo(name = "position")
    var position : Int = noteId,

    @ColumnInfo(name = "timeEdited")
    var timeEdited: Long =0L,

    @ColumnInfo(name = "timeReminder")
    var timeReminder : Long = 0L,

    @ColumnInfo(name = "state")
    var state : Int = 0
    )







