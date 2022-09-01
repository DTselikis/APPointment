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

private const val SAME_DATE = 0

@BindingAdapter("date")
fun bindDate(materialTextView: MaterialTextView, timestamp: Timestamp?) {
    timestamp?.let {
        val pattern = if (it.compareTo(Timestamp.now()) == SAME_DATE) "HH:mm" else "MM dd"
        materialTextView.text =
            SimpleDateFormat(pattern, Locale.getDefault()).format(timestamp.toDate())
    }
}