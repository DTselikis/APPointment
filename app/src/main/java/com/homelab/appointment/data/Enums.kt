package com.homelab.appointment.data

enum class Gender(val code: String) {
    FEMALE("F"),
    MALE("M"),
    ANY("A")
}

enum class LinkType(val code: Int) {
    EMAIL_VERIFICATION(1)
}