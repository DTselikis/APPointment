package gr.evasscissors.appointment.ui.auth.reauth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class ReAuthViewModel : ViewModel() {
    val password = MutableLiveData<String>()

    private val _authenticated = MutableSharedFlow<Boolean>()
    val authenticated: SharedFlow<Boolean> = _authenticated

    fun reAuthenticate(email: String) {
        FirebaseAuth.getInstance().currentUser?.reauthenticate(
            EmailAuthProvider.getCredential(
                email,
                password.value!!
            )
        )?.addOnCompleteListener { task ->
            viewModelScope.launch {
                _authenticated.emit(task.isSuccessful)
            }
        }
    }
}