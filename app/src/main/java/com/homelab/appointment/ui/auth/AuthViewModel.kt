package com.homelab.appointment.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.homelab.appointment.data.USERS_COLLECTION
import com.homelab.appointment.data.USER_DOCUMENT_TOKEN_FIELD
import com.homelab.appointment.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _userStored = MutableSharedFlow<Boolean>()
    val userStored: SharedFlow<Boolean> = _userStored

    private val _userFetched = MutableSharedFlow<Boolean>()
    val userFetched:SharedFlow<Boolean> = _userFetched

    private val _fcmStored = MutableSharedFlow<Boolean>()
    val fcmStored:SharedFlow<Boolean> = _fcmStored

    var user: User? = null
    private set

    fun storeUserToDb(firebaseUser: FirebaseUser) {
        user = firebaseUser.toUser()

        Firebase.firestore.collection(USERS_COLLECTION).document(user!!.uid!!)
            .set(user!!)
            .addOnCompleteListener { task ->
                this.user = user
                viewModelScope.launch {
                    _userStored.emit(task.isSuccessful)
                }
            }
    }

    fun fetchUser(uid: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(uid)
            .get()
            .addOnCompleteListener { doc ->
                user = doc.result.toObject<User>()

                viewModelScope.launch {
                    _userFetched.emit(true)
                }
            }
    }

    fun storeFcmToken(token: String) {
        Firebase.firestore.collection(USERS_COLLECTION).document(user!!.uid!!)
            .update(mapOf(USER_DOCUMENT_TOKEN_FIELD to token))
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    _fcmStored.emit(task.isSuccessful)
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