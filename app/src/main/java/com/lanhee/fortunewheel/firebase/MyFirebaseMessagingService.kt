package com.lanhee.fortunewheel.firebase

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Toast.makeText(baseContext, "token >> $token", Toast.LENGTH_SHORT).show()
    }
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Handler(Looper.getMainLooper()).post{
            Toast.makeText(baseContext, "message received", Toast.LENGTH_SHORT).show()
        }

        if(message.data.isNotEmpty()) {

        }

        message.notification?.let {

        }
    }

}