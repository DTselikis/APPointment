package gr.evasscissors.appointment.ui.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {
    private val _emailVerified = MutableSharedFlow<Boolean>()
    val emailVerified:SharedFlow<Boolean> = _emailVerified

    fun checkEmailVerified() {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnSuccessListener {
            viewModelScope.launch {
                _emailVerified.emit(FirebaseAuth.getInstance().currentUser!!.isEmailVerified)
            }
        }
    }
}