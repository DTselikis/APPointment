package com.homelab.appointment.ui.hub.info

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.R
import com.homelab.appointment.data.*
import com.homelab.appointment.model.BusinessInfo
import com.homelab.appointment.model.ContactProviderInfo
import com.homelab.appointment.model.helper.DayOpeningHours
import com.homelab.appointment.model.helper.SocialInfo
import com.homelab.appointment.utils.ContactProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BusinessInfoViewModel : ViewModel() {
    private val _openingHours = MutableLiveData<List<DayOpeningHours>>()
    val openingHours: LiveData<List<DayOpeningHours>> = _openingHours

    private val _infoFetched = MutableSharedFlow<Boolean>()
    val infoFetched: SharedFlow<Boolean> = _infoFetched

    private lateinit var socialInfo: SocialInfo

    fun fetchBusinessInfo() {
        Firebase.firestore.collection(BUSINESS_INFO_COLLECTION).document(BUSINESS_DOCUMENT)
            .get()
            .addOnSuccessListener { doc ->
                val businessInfo = doc.toObject<BusinessInfo>()

                _openingHours.value = businessInfo!!.opening_hours!!
                socialInfo = businessInfo.social_info!!

                viewModelScope.launch {
                    _infoFetched.emit(true)
                }
            }
    }

    fun getProvidersList(context: Context): List<ContactProviderInfo> =
        listOf(
            ContactProviderInfo(R.color.phone_green, R.drawable.ic_phone_24, socialInfo.phone!!) {
                ContactProvider.callBusiness(context, socialInfo.phone!!)
            },
            ContactProviderInfo(R.color.maps_red, R.drawable.ic_place_24, BUSINESS_NAME) {
                ContactProvider.navigateToBusiness(context, socialInfo.maps_query!!)
            },
            ContactProviderInfo(R.color.fb_blue, R.drawable.facebook_logo, FB_PAGE_NAME) {
                ContactProvider.openFacebookPage(context, socialInfo.fb_page_id!!)
            },
            ContactProviderInfo(
                R.color.insta_orange,
                R.drawable.instagram_logo,
                INSTAGRAM_PROFILE
            ) {
                ContactProvider.openInstagramPage(context, socialInfo.instagram_profile!!)
            },
            ContactProviderInfo(
                R.color.fb_messenger_blue,
                R.drawable.fb_messenger_logo,
                socialInfo.fb_page_id!!
            ) {
                ContactProvider.chatOnFacebook(context, socialInfo.fb_page_id!!)
            }

        )
}