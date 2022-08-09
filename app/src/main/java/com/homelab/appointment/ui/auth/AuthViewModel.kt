package com.homelab.appointment.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.USERS_COLLECTION
import com.homelab.appointment.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _userStored = MutableSharedFlow<Boolean>()
    val userStored: SharedFlow<Boolean> = _userStored

    fun storeUserToDb(firebaseUser: FirebaseUser) {
        val user = firebaseUser.toUser()

        Firebase.firestore.collection(USERS_COLLECTION).document(user.uid!!)
            .set(user)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    _userStored.emit(task.isSuccessful)
                }
            }
    }

    private fun FirebaseUser.toUser(): User {
        val uid = this.uid
        val nameParts = this.displayName!!.split(' ')
        val (firstname, lastname) = when (nameParts.size) {
            1 -> Pair(nameParts[0], null)
            else -> Pair(nameParts[0], nameParts[1])
        }
        val nickname = defaultNickname(firstname, lastname)
        val phone = this.phoneNumber
        val email = this.email
        val profilePic = if (this.photoUrl != null) this.photoUrl.toString() else null

        return User(
            uid = uid,
            firstname = firstname,
            lastname = lastname,
            nickname = nickname,
            phone = phone,
            email = email,
            registered = true,
            profilePic = profilePic
        )
    }

    private fun defaultNickname(firstname: String, lastname: String?): String {
        var nickname = firstname
        if (!lastname.isNullOrBlank()) {
            nickname += " $lastname"
        }

        return nickname
    }
}