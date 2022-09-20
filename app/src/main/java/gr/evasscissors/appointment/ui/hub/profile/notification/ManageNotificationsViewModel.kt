package gr.evasscissors.appointment.ui.hub.profile.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import gr.evasscissors.appointment.data.ACTIVE_NOTIFICATIONS_FIELD
import gr.evasscissors.appointment.data.NOTIFICATIONS_COLLECTION
import gr.evasscissors.appointment.data.NOTIFICATION_FIELD
import gr.evasscissors.appointment.data.USERS_COLLECTION
import gr.evasscissors.appointment.model.Notification
import gr.evasscissors.appointment.model.helper.NotificationsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageNotificationsViewModel : ViewModel() {
    companion object {
        private const val ONE_DAY = 86400
    }

    private val _notificationsForDisplay = MutableLiveData<List<Notification>>()
    val notificationsForDisplay: LiveData<List<Notification>> = _notificationsForDisplay

    private val notifications = mutableListOf<Notification>()

    private var remainingNotifications: MutableList<Notification>? = null

    fun fetchNotifications(uid: String) {
        Firebase.firestore.collection(NOTIFICATIONS_COLLECTION).document(uid)
            .get()
            .addOnSuccessListener { doc ->
                doc.data?.let {
                    notifications.addAll(
                        doc.toObject<NotificationsList>()!!.notifications!!
                            .map { it }
                            .sortedByDescending { it.timestamp!!.seconds }
                    )
                    _notificationsForDisplay.value = notifications.toList()

                    viewModelScope.launch(Dispatchers.IO) {
                        remainingNotifications = notifications.newerThan(ONE_DAY)
                    }
                }
            }
    }

    fun deleteOldNotifications(uid: String) {
        remainingNotifications?.let {
            val db = Firebase.firestore
            db.runTransaction { transaction ->
                val userDocRef = db.collection(USERS_COLLECTION).document(uid)
                val userNotificationsDocRef = db.collection(NOTIFICATIONS_COLLECTION).document(uid)

                transaction.update(userDocRef, mapOf(ACTIVE_NOTIFICATIONS_FIELD to 0))

                transaction.apply {
                    if (it.isEmpty()) {
                        delete(userNotificationsDocRef)
                    } else {
                        set(
                            userNotificationsDocRef,
                            mapOf(NOTIFICATION_FIELD to remainingNotifications),
                            SetOptions.merge()
                        )
                    }
                }
            }
        }
    }

    fun deleteNotification(notification: Notification) {
        remainingNotifications?.remove(notification)
        _notificationsForDisplay.value = remainingNotifications?.toList()
    }

    private fun List<Notification>.newerThan(days: Int): MutableList<Notification> {
        val now = Timestamp.now().seconds
        val (_, remaining) = this.partition {
            it.status = ManageNotificationsFragment.NotificationStatus.READ.code

            now - it.timestamp!!.seconds > days
        }

        return remaining.toMutableList()
    }
}