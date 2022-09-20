package gr.evasscissors.appointment.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Notification(
    val title: String? = null,
    val message: String? = null,
    val type: Int? = null,
    var status: Int? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null
)