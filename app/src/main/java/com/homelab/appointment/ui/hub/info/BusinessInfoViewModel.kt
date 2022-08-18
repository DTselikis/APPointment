package com.homelab.appointment.ui.hub.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.BUSINESS_DOCUMENT
import com.homelab.appointment.data.BUSINESS_INFO_COLLECTION
import com.homelab.appointment.model.BusinessInfo
import com.homelab.appointment.model.helper.OpeningHours
import com.homelab.appointment.model.helper.SocialInfo

class BusinessInfoViewModel : ViewModel() {
    private val _openingHours = MutableLiveData<List<OpeningHours>>()
    val openingHours:LiveData<List<OpeningHours>> = _openingHours

    private lateinit var socialInfo: SocialInfo

    fun fetchBusinessInfo() {
        Firebase.firestore.collection(BUSINESS_INFO_COLLECTION).document(BUSINESS_DOCUMENT)
            .get()
            .addOnSuccessListener { doc ->
                val businessInfo = doc.toObject<BusinessInfo>()

                _openingHours.value = businessInfo!!.openingHours!!
                socialInfo = businessInfo.socialInfo!!
            }
    }
}