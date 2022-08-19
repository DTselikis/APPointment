package com.homelab.appointment.ui.hub.info

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.homelab.appointment.R
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

@BindingAdapter("hours")
fun bindHours(materialTextView: MaterialTextView, dayOpeningHours: DayOpeningHours?) {
    val resources = materialTextView.resources

    var morningHours: String? = null
    dayOpeningHours!!.morning?.let {
        morningHours = resources.getString(R.string.opening_hours, it.from, it.to)
    }

    var afternoonHours: String? = null
    dayOpeningHours.afternoon?.let {
        afternoonHours = resources.getString(R.string.opening_hours, it.from, it.to)
    }

    materialTextView.text = if (morningHours == null && afternoonHours == null) {
        resources.getString(R.string.store_closed)
    } else {
        "${morningHours ?: ""}\n${afternoonHours ?: ""}"
    }
}