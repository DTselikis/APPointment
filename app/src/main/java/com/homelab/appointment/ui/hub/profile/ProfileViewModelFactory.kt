package com.homelab.appointment.ui.hub.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homelab.appointment.model.User

class ProfileViewModelFactory(
    private val user: User
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(user) as T
    }
}