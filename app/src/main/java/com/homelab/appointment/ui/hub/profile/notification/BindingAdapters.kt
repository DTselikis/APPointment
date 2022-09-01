package com.homelab.appointment.ui.hub.profile.notification

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homelab.appointment.model.Notification

@BindingAdapter("notifications")
fun bindNotifications(recyclerView: RecyclerView, notifications: List<Notification>?) {
    notifications?.let {
        val adapter = recyclerView.adapter as NotificationsAdapter

        adapter.submitList(notifications)
    }
}