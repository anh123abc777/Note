package com.example.keep.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.keep.R
import com.example.keep.database.NoteDatabase
import com.example.keep.database.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ReminderBroadcast: BroadcastReceiver() {
    private var notificationId = 0
    private var content = ""
    private var title = ""

    override fun onReceive(context: Context?, intent: Intent?) {

        val extras = intent!!.extras
        notificationId = extras!!.getInt("id")
        title = extras!!.getString("title")!!
        content = extras!!.getString("content")!!
        val timeReminder = extras!!.getLong("timeReminder")

        val repository = NoteRepository(NoteDatabase.getInstance(context!!).noteDao)
        runBlocking {
            withContext(Dispatchers.IO){
                var noteAddReminder = repository.get(notificationId)
                noteAddReminder.note.timeReminder = timeReminder
                repository.update(noteAddReminder.note)
            }
        }
        setUpNotification(context!!)

    }


    private fun setUpNotification(context: Context){

        val builder = NotificationCompat.Builder(context,"notifyId")
            .setSmallIcon(R.drawable.ic_outline_notifications_24)
            .setContentText(content)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId,builder.build())
    }

}