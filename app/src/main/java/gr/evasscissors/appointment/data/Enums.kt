package gr.evasscissors.appointment.data

enum class Gender(val code: String) {
    FEMALE("F"),
    MALE("M"),
    ANY("A")
}

enum class LinkType(val code: Int) {
    EMAIL_VERIFICATION(1)
}

enum class DayOfWeek(val code: Int) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7)
}

enum class ExtAppName(val code: String) {
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram")
}