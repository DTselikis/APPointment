package com.homelab.appointment.ui.auth

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.homelab.appointment.R
import com.homelab.appointment.data.SHARED_PREF_FCM_KEY
import com.homelab.appointment.data.SHARED_PREF_NAME
import kotlinx.coroutines.flow.collectLatest

class AuthFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()

    private var isNewUser = false

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createSignInIntent()
    }

    private fun createSignInIntent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(intent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (signInSuccessfully(result)) {
            isNewUser = result.idpResponse!!.isNewUser

            val firebaseUser = FirebaseAuth.getInstance().currentUser

            firebaseUser?.let {
                if (isNewUser) {
                    observeUserStored()
                    viewModel.storeUserToDb(it)
                } else {
                    observeUserFetched()
                    viewModel.fetchUser(it.uid)
                }
            }
        }
    }

    private fun navigateToProfile() {
        val action = AuthFragmentDirections.actionAuthFragmentToProfileFragment(viewModel.user!!)
        findNavController().navigate(action)
    }

    private fun navigateToBusinessInfo() {
        findNavController().navigate(R.id.action_authFragment_to_businessInfoFragment)
    }

    private fun observeUserStored() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userStored.collectLatest { stored ->
                if (stored) {
                    storeFcmTokenIfNotStored { navigateToProfile() }
                }
            }
        }
    }

    private fun observeUserFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userStored.collectLatest { fetched ->
                storeFcmTokenIfNotStored { navigateToBusinessInfo() }
            }
        }
    }

    private fun observeFcmTokenStored(navigate: () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fcmStored.collectLatest { stored ->
                with(
                    requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                ) {
                    remove(SHARED_PREF_FCM_KEY)
                    apply()
                }

                navigate()
            }
        }
    }

    private fun storeFcmTokenIfNotStored(navigate: () -> Unit) {
        requireActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getString(
                SHARED_PREF_FCM_KEY, null
            )?.let { token ->
                observeFcmTokenStored(navigate)
                viewModel.storeFcmToken(token)
            }
    }

    private fun signInSuccessfully(result: FirebaseAuthUIAuthenticationResult): Boolean =
        result.resultCode == Activity.RESULT_OK

}