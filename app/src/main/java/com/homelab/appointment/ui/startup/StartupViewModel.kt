package com.homelab.appointment.ui.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.USERS_COLLECTION
import com.homelab.appointment.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class StartupViewModel : ViewModel() {
    private val _userFetched = MutableSharedFlow<Boolean>()
    val userFetched: SharedFlow<Boolean> = _userFetched

    private lateinit var user: User

    fun fetchUser(uid: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(uid)
            .get()
            .addOnCompleteListener { doc ->
                user = doc.result.toObject<User>()!!

                viewModelScope.launch {
                    _userFetched.emit(true)
                }
            }
    }
}