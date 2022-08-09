package com.homelab.appointment.model

data class User(
    var uid: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val fbName: String? = null,
    val token: String? = null,
    val profilePic: String? = null,
    val gender: String? = null,
    val registered: Boolean = false
)