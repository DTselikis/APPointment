package com.homelab.appointment.ui.hub.profile.emailDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class VerificationEmailDialogViewModel : ViewModel() {
    private val _emailVerified = MutableSharedFlow<Boolean>()
    val emailVerified: SharedFlow<Boolean> = _emailVerified

    fun checkEmailVerified(newEmail: String) {
        FirebaseAuth.getInstance().currentUser?.reload()
            ?.addOnSuccessListener {
                viewModelScope.launch {
                    _emailVerified.emit(FirebaseAuth.getInstance().currentUser?.email == newEmail)
                }
            }
    }
}