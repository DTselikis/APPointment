package com.homelab.appointment.model

import com.homelab.appointment.model.helper.DayOpeningHours
import com.homelab.appointment.model.helper.SocialInfo

data class BusinessInfo(
    val opening_hours: List<DayOpeningHours>? = null,
    val social_info: SocialInfo? = null
)