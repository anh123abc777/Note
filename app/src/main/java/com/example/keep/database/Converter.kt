package com.example.keep.database

import android.net.Uri
import androidx.room.TypeConverter
import com.example.keep.checkbox.CheckboxGroupAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class Converter{
    @TypeConverter
    fun fromString(value : String) : ArrayList<String>{

        val listType = object: TypeToken<ArrayList<String>>(){}.type
        return Gson().fromJson(value,listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>) : String{
        return Gson().toJson(list)
    }
}

object MapTypeConverter {
    @TypeConverter
    @JvmStatic
    fun stringToMap(value: String): ArrayList<DataCheckboxes> {
        return Gson().fromJson(value,  object : TypeToken<List<DataCheckboxes>>() {}.type)
    }

    @TypeConverter
    @JvmStatic
    fun mapToString(value: ArrayList<DataCheckboxes>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }
}
