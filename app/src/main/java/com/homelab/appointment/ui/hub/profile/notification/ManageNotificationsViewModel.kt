package com.homelab.appointment.ui.hub.profile.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.NOTIFICATIONS_COLLECTION
import com.homelab.appointment.model.Notification
import com.homelab.appointment.model.helper.NotificationsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageNotificationsViewModel : ViewModel() {
    private val _notificationsForDisplay = MutableLiveData<List<Notification>>()
    val notificationsForDisplay: LiveData<List<Notification>> = _notificationsForDisplay

    private val notifications = mutableListOf<Notification>()

    private lateinit var notificationsRemaining: List<Notification>

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
                    notificationsRemaining = notifications.olderThan(1)
                }
            }
            .addOnFailureListener {
                Log.i("Notifications", "fetchNotifications: There are no notifications")
            }
    }

    private fun List<Notification>.olderThan(days: Int): List<Notification> {
        val today = Timestamp.now().toDate()
        val (_, remaining) = this.partition {
            it.timestamp!!.toDate().time - today.time >= days
        }

        return remaining
    }
}