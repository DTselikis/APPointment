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
            }
        )
}