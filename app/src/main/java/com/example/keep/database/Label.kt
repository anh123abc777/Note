package com.example.keep.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "label_table")
data class Label(
    @PrimaryKey(autoGenerate = true)
    var labelId : Int,

    @ColumnInfo(name = "labelName")
    var labelName : String = "labelName"
){
    constructor(label: String) : this(0,label) {
        this.labelName = label
    }
}
