package com.example.keep.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Note::class,Label::class,NoteLabelCrossRef::class], version = 2, exportSchema = false)
//@TypeConverters(Converter::class)
@TypeConverters(MapTypeConverter::class,Converter::class)
abstract class NoteDatabase : RoomDatabase(){

    abstract val noteDao :NoteDao
    abstract val labelDao: LabelDao

    companion object{

        @Volatile
        private var INSTANCE : NoteDatabase? = null

        fun getInstance(context: Context) : NoteDatabase{
            synchronized(this) {
                var instance = INSTANCE

                if (instance==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java,
                        "note_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}