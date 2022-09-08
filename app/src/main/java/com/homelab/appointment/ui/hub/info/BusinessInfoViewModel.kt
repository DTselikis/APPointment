package com.homelab.appointment.ui.hub.info

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.R
import com.homelab.appointment.data.BUSINESS_DOCUMENT
import com.homelab.appointment.data.BUSINESS_INFO_COLLECTION
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

    private val _textToCopy = MutableSharedFlow<String>()
    val textToCopy: SharedFlow<String> = _textToCopy

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

    fun getProvidersList(context: Context): List<ContactProviderInfo> {
        val contactProviders = mutableListOf<ContactProviderInfo>()

        socialInfo.phone?.let {
            contactProviders.add(
                ContactProviderInfo(
                    R.color.phone_green,
                    R.drawable.ic_phone_24,
                    it,
                    null,
                    { setTextToBeCopied(it) }
                ) { ContactProvider.callBusiness(context, it) }
            )
        }

        socialInfo.maps_query?.let {
            contactProviders.add(
                ContactProviderInfo(
                    R.color.maps_red,
                    R.drawable.ic_place_24,
                    socialInfo.mapsName!!,
                    null,
                    { setTextToBeCopied(it) }
                ) { ContactProvider.navigateToBusiness(context, it) }
            )
        }

        socialInfo.fb_page_id?.let {
            contactProviders.add(
                ContactProviderInfo(
                    R.color.fb_blue,
                    R.drawable.facebook_logo,
                    socialInfo.fbPageName!!,
                    null,
                    { setTextToBeCopied(socialInfo.fbPageUniqueName!!) }
                ) { ContactProvider.openFacebookPage(context, it) }
            )
            contactProviders.add(
                ContactProviderInfo(
                    R.color.fb_messenger_blue,
                    R.drawable.fb_messenger_logo,
                    socialInfo.fbPageName!!,
                    AppCompatResources.getDrawable(context, R.drawable.fb_messenger_background),
                    { setTextToBeCopied(socialInfo.fbPageUniqueName!!) }
                ) { ContactProvider.chatOnFacebook(context, it) }
            )
        }

        socialInfo.instagram_profile?.let {
            contactProviders.add(ContactProviderInfo(
                R.color.insta_orange,
                R.drawable.instagram_logo,
                "@$it",
                AppCompatResources.getDrawable(context, R.drawable.instagram_background),
                { setTextToBeCopied(it) }
            ) { ContactProvider.openInstagramPage(context, it) }
            )
        }

        return contactProviders.toList()
    }

    private fun setTextToBeCopied(text: String): Boolean {
        viewModelScope.launch {
            _textToCopy.emit(text)
        }

        return true
    }
}