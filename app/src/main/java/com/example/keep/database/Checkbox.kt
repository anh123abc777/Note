package com.example.keep.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkbox_table")
data class Checkbox(
    @PrimaryKey(autoGenerate = true)
    var checkboxId : Int,

    @ColumnInfo(name = "label")
    var label : String = "",

    @ColumnInfo(name = "checked")
    var checked : Boolean = false,

    @ColumnInfo(name = "ownerId")
    val ownerId : Int

){
    constructor(label: String,ownerId: Int) : this(0,label,ownerId = ownerId) {
    }
}
