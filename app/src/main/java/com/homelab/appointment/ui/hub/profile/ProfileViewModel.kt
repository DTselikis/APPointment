package com.homelab.appointment.ui.hub.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.homelab.appointment.data.STORAGE_PROFILE_PIC_DIR
import com.homelab.appointment.data.USERS_COLLECTION
import com.homelab.appointment.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(val user: User) : ViewModel() {
    val firstname = MutableLiveData<String>(user.firstname)
    val lastname = MutableLiveData<String>(user.lastname)
    val phone = MutableLiveData<String>(user.phone)
    val email = MutableLiveData<String>(user.email)
    val fbName = MutableLiveData<String>(user.fbName)
    val profilePic = MutableLiveData(Pair(user.profilePic, user.gender))

    private val _picUploaded = MutableSharedFlow<Boolean>()
    val picUploaded: SharedFlow<Boolean> = _picUploaded

    private val _needsReAuth = MutableSharedFlow<Boolean>()
    val needsReAuth: SharedFlow<Boolean> = _needsReAuth

    private val _verificationEmailSent = MutableSharedFlow<Boolean>()
    val verificationEmailSent: SharedFlow<Boolean> = _verificationEmailSent

    private val _updatedEmailStored = MutableSharedFlow<Boolean>()
    val updatedEmailStored: SharedFlow<Boolean> = _updatedEmailStored

    private val _updatedPhoneStored = MutableSharedFlow<Boolean>()
    val updatedPhoneStored: SharedFlow<Boolean> = _updatedPhoneStored

    fun storeImageToFirebase(image: File) {
        try {
            val ref =
                Firebase.storage.reference.child("$STORAGE_PROFILE_PIC_DIR/${user.uid}${image.extension}")
            ref.putFile(Uri.fromFile(image))
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    val newUrl = mapOf("profilePic" to task.result.toString())

                    Firebase.firestore.collection(USERS_COLLECTION).document(user.uid!!)
                        .update(newUrl)
                }
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        _picUploaded.emit(task.isSuccessful)
                    }
                }
        } catch (e: Exception) {
            viewModelScope.launch {
                _picUploaded.emit(false)
            }
        }
    }

    fun verifyNewEmail() {
        FirebaseAuth.getInstance().currentUser?.verifyBeforeUpdateEmail(email.value!!)
            ?.addOnSuccessListener {
                viewModelScope.launch {
                    _verificationEmailSent.emit(true)
                }
            }
            ?.addOnFailureListener { e ->
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    viewModelScope.launch {
                        _needsReAuth.emit(true)
                    }
                }
            }
    }

    fun storeUpdatedPhone() {
        Firebase.firestore.collection(USERS_COLLECTION).document(user.uid!!)
            .update(mapOf("phone" to phone.value))
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    _updatedPhoneStored.emit(task.isSuccessful)
                }
            }
    }

    fun storeUpdatedEmail() {
        val updatedEmail = FirebaseAuth.getInstance().currentUser?.email
        Firebase.firestore.collection(USERS_COLLECTION).document(user.uid!!)
            .update(mapOf("email" to updatedEmail))
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    _updatedEmailStored.emit(task.isSuccessful)
                }
            }
    }

    fun updateProfilePic(path: String) {
        profilePic.value = Pair(path, user.gender)
    }
}