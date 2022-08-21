package com.homelab.appointment.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class ContactProviderInfo(
    @ColorRes
    val color: Int,
    @DrawableRes
    val icon: Int,
    val text: String,
    val operation: () -> Unit
)