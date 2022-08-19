package com.homelab.appointment.ui.hub.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homelab.appointment.databinding.DayOpeningHoursItemBinding
import com.homelab.appointment.model.helper.DayOpeningHours

class OpeningHoursAdapter :
    ListAdapter<DayOpeningHours, OpeningHoursAdapter.OpeningHoursViewHolder>(DiffCallback) {

    inner class OpeningHoursViewHolder(
        private val binding: DayOpeningHoursItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dayOpeningHours: DayOpeningHours) {
            binding.dayOpeningHours = dayOpeningHours
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DayOpeningHours>() {
        override fun areItemsTheSame(oldItem: DayOpeningHours, newItem: DayOpeningHours): Boolean {
            return oldItem.order == newItem.order
        }

        override fun areContentsTheSame(
            oldItem: DayOpeningHours,
            newItem: DayOpeningHours
        ): Boolean {
            return oldItem.morning?.from == newItem.morning?.from && oldItem.morning?.to == newItem.morning?.to
                    && oldItem.afternoon?.from == newItem.afternoon?.from && oldItem.afternoon?.to == newItem.afternoon?.to
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpeningHoursViewHolder {
        return OpeningHoursViewHolder(DayOpeningHoursItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: OpeningHoursViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}