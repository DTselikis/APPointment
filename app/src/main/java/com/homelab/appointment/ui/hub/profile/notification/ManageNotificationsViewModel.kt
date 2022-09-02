package com.homelab.appointment.ui.hub.profile.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.NOTIFICATIONS_COLLECTION
import com.homelab.appointment.data.NOTIFICATION_FIELD
import com.homelab.appointment.model.Notification
import com.homelab.appointment.model.helper.NotificationsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageNotificationsViewModel : ViewModel() {
    private val _notificationsForDisplay = MutableLiveData<List<Notification>>()
    val notificationsForDisplay: LiveData<List<Notification>> = _notificationsForDisplay

    private val notifications = mutableListOf<Notification>()

    private lateinit var remainingNotifications: MutableList<Notification>

    fun fetchNotifications(uid: String) {
        Firebase.firestore.collection(NOTIFICATIONS_COLLECTION).document(uid)
            .get()
            .addOnSuccessListener { doc ->
                notifications.addAll(
                    doc.toObject<NotificationsList>()!!.notifications!!
                        .map { it }
                        .sortedByDescending { it.timestamp!!.seconds }
                )
                _notificationsForDisplay.value = notifications.toList()

                viewModelScope.launch(Dispatchers.IO) {
                    remainingNotifications = notifications.newerThan(0)
                }
            }
            .addOnFailureListener {
                Log.i("Notifications", "fetchNotifications: There are no notifications")
            }
    }

    fun deleteOldNotifications(uid: String) {
        Firebase.firestore.collection(NOTIFICATIONS_COLLECTION).document(uid).apply {
            if (remainingNotifications.isEmpty()) {
                delete()
            } else {
                set(mapOf(NOTIFICATION_FIELD to remainingNotifications), SetOptions.merge())
            }
        }
    }

    fun deleteNotification(notification: Notification) {
        remainingNotifications.remove(notification)
    }

    private fun List<Notification>.newerThan(days: Int): MutableList<Notification> {
        val today = Timestamp.now().toDate().time
        val (_, remaining) = this.partition {
            (today - it.timestamp!!.toDate().time) / (24 * 60 * 60 * 1000) >= days
        }

        return remaining.toMutableList()
    }
}