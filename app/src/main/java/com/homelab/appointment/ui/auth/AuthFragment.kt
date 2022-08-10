package com.homelab.appointment.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentAuthBinding
import com.homelab.appointment.ui.hub.HubSharedViewModel
import kotlinx.coroutines.flow.collectLatest

class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private val sharedViewModel: HubSharedViewModel by activityViewModels()
    private lateinit var binding: FragmentAuthBinding

    private var isNewUser = false

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth, null, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            authFragment = this@AuthFragment
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAuth.getInstance().signOut()
        AuthUI.getInstance().signOut(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        if (emailVerificationLinkClicked()) {
            val isNew = extractParameterFromLink()
            if (isNew) {
                navigateToProfile()
            } else {
                // TODO navigate to hub
            }
        } else {
            if (isUserLoggedIn(user)) {
                user?.let {
                    if (it.isEmailVerified) {
                        // TODO navigate to hub
                    } else {
                        verifyEmail(it, false)
                    }
                }
            } else {
                createSignInIntent()
            }
        }

        observeUserStored()
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
            val user = FirebaseAuth.getInstance().currentUser
            isNewUser = result.idpResponse!!.isNewUser

            user?.let {
                if (isNewUser) {
                    viewModel.storeUserToDb(it)
                } else {
                    if (it.isEmailVerified) {
                        // TODO navigate to hub
                    } else {
                        verifyEmail(it, isNewUser)
                    }
                }
            }

        }
    }

    private fun verifyEmail(user: FirebaseUser, newUser: Boolean) {
        binding.emailVerificationGroup.visibility = View.VISIBLE
        user.sendEmailVerification(
            ActionCodeSettings.newBuilder()
                .setUrl("https://homelab.page.link/emailVerified?isNewUser=$newUser")
                .setAndroidPackageName("com.homelab.appointment", true, null)
                .setHandleCodeInApp(true)
                .setDynamicLinkDomain("homelab.page.link")
                .build()
        )
    }

    private fun navigateToProfile() {
        sharedViewModel.user = viewModel.user
        findNavController().navigate(R.id.action_authFragment_to_profileFragment)
    }

    fun checkEmailVerified() {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnSuccessListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                if (user.isEmailVerified) {
                    if (isNewUser) {
                        navigateToProfile()
                    } else {
                        // TODO navigate to profile
                    }
                }
            }
        }
    }

    fun resendEmail() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            verifyEmail(it, isNewUser)
        }

        Toast.makeText(
            context,
            getString(R.string.resend_verification_email_toast),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun observeUserStored() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userStored.collectLatest { stored ->
                if (stored) {
                    val user = FirebaseAuth.getInstance().currentUser

                    user?.let {
                        if (it.isEmailVerified) {
                            navigateToProfile()
                        } else {
                            verifyEmail(user, isNewUser)
                        }
                    }
                } else {
                    // TODO show message if not stored
                }
            }
        }
    }

    private fun extractParameterFromLink(): Boolean {
        val link = requireActivity().intent.data
        link?.let {
            val url = it.getQueryParameter("continueUrl")
            return url!!.substring(url.lastIndexOf('=') + 1, url.length).toBoolean()
        }
        return false
    }

    private fun emailVerificationLinkClicked(): Boolean = requireActivity().intent.extras != null

    private fun signInSuccessfully(result: FirebaseAuthUIAuthenticationResult): Boolean =
        result.resultCode == Activity.RESULT_OK

    private fun isUserLoggedIn(user: FirebaseUser?): Boolean = user != null

}