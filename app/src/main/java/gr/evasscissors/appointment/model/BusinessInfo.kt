package gr.evasscissors.appointment.model

import gr.evasscissors.appointment.model.helper.DayOpeningHours
import gr.evasscissors.appointment.model.helper.SocialInfo

data class BusinessInfo(
    val opening_hours: List<DayOpeningHours>? = null,
    val social_info: SocialInfo? = null
)