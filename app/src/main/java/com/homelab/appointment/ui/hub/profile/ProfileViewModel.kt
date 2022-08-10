package com.homelab.appointment.ui.hub.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.homelab.appointment.model.User

class ProfileViewModel(val user: User) : ViewModel() {
    val firstname = MutableLiveData<String>(user.firstname)
    val lastname = MutableLiveData<String>(user.lastname)
    val phone = MutableLiveData<String>(user.phone)
    val email = MutableLiveData<String>(user.email)
    val fbName = MutableLiveData<String>(user.fbName)
}