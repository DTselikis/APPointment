package gr.evasscissors.appointment.model.helper

data class DayOpeningHours(
    val morning: OpeningHours? = null,
    val afternoon: OpeningHours? = null,
    val order: Int? = null
)