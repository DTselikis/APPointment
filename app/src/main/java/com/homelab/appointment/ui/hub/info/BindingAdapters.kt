package com.homelab.appointment.ui.hub.info

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.homelab.appointment.data.daysOfWeek
import com.homelab.appointment.data.orderToDayOfWeek
import com.homelab.appointment.model.helper.DayOpeningHours

@BindingAdapter("openingHours")
fun bindOpeningHours(recyclerView: RecyclerView, openingHours: List<DayOpeningHours>?) {
    val adapter = recyclerView.adapter as OpeningHoursAdapter

    val sortedList = openingHours?.sortedWith(compareBy { it.order })

    adapter.submitList(sortedList)
}

@BindingAdapter("day")
fun bindDate(materialTextView: MaterialTextView, day: Int?) {
   val dayOfWeek = orderToDayOfWeek[day]

    materialTextView.text = materialTextView.resources.getString(daysOfWeek[dayOfWeek]!!)
}