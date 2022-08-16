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

    private val _emailUpdated = MutableSharedFlow<Boolean>()
    val emailUpdated: SharedFlow<Boolean> = _emailUpdated

    lateinit var user: User
    private set

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

    fun updateEmail(newEmail: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(user.uid!!)
            .update(mapOf("email" to newEmail))
            .addOnCompleteListener {
                viewModelScope.launch {
                    _emailUpdated.emit(true)
                }
            }
    }
}