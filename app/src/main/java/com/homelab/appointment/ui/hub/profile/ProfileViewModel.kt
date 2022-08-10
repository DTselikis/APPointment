package com.homelab.appointment.ui.hub.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.homelab.appointment.data.STORAGE_PROFILE_PIC_DIR
import com.homelab.appointment.model.User
import java.io.File

class ProfileViewModel(private val user: User) : ViewModel() {
    val firstname = MutableLiveData<String>(user.firstname)
    val lastname = MutableLiveData<String>(user.lastname)
    val phone = MutableLiveData<String>(user.phone)
    val email = MutableLiveData<String>(user.email)
    val fbName = MutableLiveData<String>(user.fbName)
    val profilePic = MutableLiveData(Pair(user.profilePic, user.gender))

    fun storeImageToFirebase(image: File) {
        Firebase.storage.reference.child("$STORAGE_PROFILE_PIC_DIR/${user.uid}${image.extension}")
            .putFile(Uri.fromFile(image))
            .addOnSuccessListener { uploadTask ->

            }
            .addOnFailureListener {

            }
    }
}