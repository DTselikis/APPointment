package com.homelab.appointment.ui.hub.profile.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.NOTIFICATIONS_COLLECTION
import com.homelab.appointment.model.Notification
import com.homelab.appointment.model.helper.NotificationsList

class ManageNotificationsViewModel : ViewModel() {
    private val notifications = mutableListOf<Notification>()

    fun fetchNotifications(uid: String) {
        Firebase.firestore.collection(NOTIFICATIONS_COLLECTION).document(uid)
            .get()
            .addOnSuccessListener { doc ->
                notifications.addAll(doc.toObject<NotificationsList>()!!.notifications!!.map { it })
            }
            .addOnFailureListener {
                Log.i("Notifications", "fetchNotifications: There are no notifications")
            }
    }
}