package com.homelab.appointment.data

import com.homelab.appointment.R

val daysOfWeek = mapOf(
    DayOfWeek.MONDAY to R.string.monday,
    DayOfWeek.TUESDAY to R.string.tuesday,
    DayOfWeek.WEDNESDAY to R.string.wednesday,
    DayOfWeek.THURSDAY to R.string.thursday,
    DayOfWeek.FRIDAY to R.string.friday,
    DayOfWeek.SATURDAY to R.string.saturday,
    DayOfWeek.SUNDAY to R.string.sunday
)

val orderToDayOfWeek = mapOf(
    1 to DayOfWeek.MONDAY,
    2 to DayOfWeek.TUESDAY,
    3 to DayOfWeek.WEDNESDAY,
    4 to DayOfWeek.THURSDAY,
    5 to DayOfWeek.FRIDAY,
    6 to DayOfWeek.SATURDAY,
    7 to DayOfWeek.SUNDAY
)