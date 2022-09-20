package gr.evasscissors.appointment.ui.auth.catcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import gr.evasscissors.appointment.data.USERS_COLLECTION
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuthLinkCatcherViewModel : ViewModel() {
    private val _emailUpdated = MutableSharedFlow<Boolean>()
    val emailUpdated: SharedFlow<Boolean> = _emailUpdated

    fun updateEmail() {
        FirebaseAuth.getInstance().currentUser?.let {
            Firebase.firestore.collection(USERS_COLLECTION).document(it.uid)
                .update(mapOf("email" to it.email))
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        _emailUpdated.emit(task.isSuccessful)
                    }
                }
        }
    }
}