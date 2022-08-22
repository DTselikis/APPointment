package com.homelab.appointment.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.homelab.appointment.R
import com.homelab.appointment.data.*

class CloudMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser.isUserSignedIn()) {
            storeFcmTokenToDb(firebaseUser!!, token)
        } else {
            storeFcmTokenToSharedPref(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        createNotificationChannel()
    }

    private fun storeFcmTokenToDb(firebaseUser: FirebaseUser, token: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            .update(mapOf(USER_DOCUMENT_TOKEN_FIELD to token))
            .addOnFailureListener {
                storeFcmTokenToSharedPref(token)
            }
    }

    private fun storeFcmTokenToSharedPref(token: String) {
        val sharedPref =
            applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(SHARED_PREF_FCM_KEY, token)
            apply()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channe_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = getString(R.string.notification_channel_descr)
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lightColor = Color.GREEN
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun FirebaseUser?.isUserSignedIn(): Boolean = this != null
}