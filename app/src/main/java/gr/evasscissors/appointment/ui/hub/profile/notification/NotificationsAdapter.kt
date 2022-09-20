package gr.evasscissors.appointment.ui.hub.profile.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.databinding.NotificationItemBinding
import gr.evasscissors.appointment.model.Notification
import gr.evasscissors.appointment.ui.hub.profile.notification.ManageNotificationsFragment.NotificationType

class NotificationsAdapter(private val fragment: ManageNotificationsFragment) :
    ListAdapter<Notification, NotificationsAdapter.NotificationViewHolder>(DiffCallback) {

    inner class NotificationViewHolder(
        private val binding: NotificationItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.notification = notification

            val indicatorColor = when (notification.type) {
                NotificationType.CANCELLATION.code -> R.color.email_red
                NotificationType.CUSTOM.code -> R.color.insta_orange
                else -> R.color.fb_blue
            }


            binding.indicatorColor = ResourcesCompat.getColor(
                fragment.resources,
                indicatorColor,
                fragment.requireActivity().theme
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.title == newItem.title && oldItem.message == newItem.message
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItemAtPosition(position: Int): Notification = getItem(position)
}