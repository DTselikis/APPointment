package com.homelab.appointment.ui.auth.reauth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReAuthViewModel : ViewModel() {
    val password = MutableLiveData<String>()
}