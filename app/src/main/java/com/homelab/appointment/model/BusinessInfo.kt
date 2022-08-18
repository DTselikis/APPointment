package com.homelab.appointment.model

import com.google.firebase.firestore.PropertyName
import com.homelab.appointment.model.helper.OpeningHours
import com.homelab.appointment.model.helper.SocialInfo

data class BusinessInfo(
    @PropertyName("opening_hours")
    val openingHours: List<OpeningHours>? = null,
    @PropertyName("social_info")
    val socialInfo: SocialInfo? = null
)