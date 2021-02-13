package com.crucialtech.uberremake

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService  : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "onMessageReceived: ${token.toString()} ")
        if(FirebaseAuth.getInstance().currentUser != null){
            UserUtils.updateToken(this,token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data

        Log.d("TAG", "onMessageReceived: ${data["title"]} ")

        Common.showNotificatio(this, Random.nextInt(),data[NOTI_TITLE],data[NOTI_BODY],null)

    }


}