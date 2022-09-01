package com.homelab.appointment.ui.hub.profile.notification

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.homelab.appointment.model.Notification
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("notifications")
fun bindNotifications(recyclerView: RecyclerView, notifications: List<Notification>?) {
    notifications?.let {
        val adapter = recyclerView.adapter as NotificationsAdapter

        adapter.submitList(notifications)
    }
}

@BindingAdapter("date")
fun bindDate(materialTextView: MaterialTextView, timestamp: Timestamp?) {
    timestamp?.let {
        val pattern = if (it.isSameDate(Timestamp.now())) "HH:mm" else "MMM d"
        materialTextView.text =
            SimpleDateFormat(pattern, Locale.getDefault()).format(timestamp.toDate())
    }
}

private fun Timestamp.isSameDate(today: Timestamp): Boolean =
    (this.toDate().time - today.toDate().time) / (24 * 60 * 60 * 1000) <= 0