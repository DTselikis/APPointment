package com.homelab.appointment.ui.hub.info

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homelab.appointment.model.helper.DayOpeningHours

@BindingAdapter("openingHours")
fun bindOpeningHours(recyclerView: RecyclerView, openingHours: List<DayOpeningHours>?) {
    val adapter = recyclerView.adapter as OpeningHoursAdapter

    val sortedList = openingHours?.sortedWith(compareBy { it.order })

    adapter.submitList(sortedList)
}