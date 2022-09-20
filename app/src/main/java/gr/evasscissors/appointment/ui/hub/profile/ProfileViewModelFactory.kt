package gr.evasscissors.appointment.ui.hub.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import gr.evasscissors.appointment.model.User

class ProfileViewModelFactory(
    private val user: User
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(user) as T
    }
}