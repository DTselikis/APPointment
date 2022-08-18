package com.homelab.appointment.model.helper

import com.google.firebase.firestore.PropertyName

data class SocialInfo(
    @PropertyName("fb_page_id")
    val fbPageId: String? = null,
    @PropertyName("instagram_profile")
    val instagramProfile: String? = null,
    @PropertyName("maps_query")
    val mapsQuery: String? = null,
    val phone: String? = null
)