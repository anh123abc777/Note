package com.example.keep.database

data class Notification (
    val notificationId : Int = 0,
    var timeReminder : Long = System.currentTimeMillis(),
        )
