package com.homelab.appointment.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.homelab.appointment.data.SHARED_PREF_FCM_KEY
import com.homelab.appointment.data.SHARED_PREF_NAME
import com.homelab.appointment.data.USERS_COLLECTION

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

    private fun storeFcmTokenToDb(firebaseUser: FirebaseUser, token: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            .update(mapOf("token" to token))
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

    private fun FirebaseUser?.isUserSignedIn(): Boolean = this != null
}