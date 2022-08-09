package com.homelab.appointment.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding

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

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAuth.getInstance().signOut()
        AuthUI.getInstance().signOut(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        if (emailVerificationLinkClicked()) {
        } else {
            if (isUserLoggedIn(user)) {
                user?.let {
                    if (it.isEmailVerified) {

                    } else {
                        verifyEmail(it)
                    }
                }
            } else {
                createSignInIntent()
            }
        }
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

            user?.let {
                if (it.isEmailVerified) {
                } else {
                    verifyEmail(it)
                }
            }



            if (result.idpResponse!!.isNewUser) {
            }
        }
    }

    private fun verifyEmail(user: FirebaseUser) {
        binding.emailVerificationGroup.visibility = View.VISIBLE
        user.sendEmailVerification(
            ActionCodeSettings.newBuilder()
                .setUrl("https://homelab.page.link/emailVerified")
                .setAndroidPackageName("com.homelab.appointment", true, null)
                .setHandleCodeInApp(true)
                .setDynamicLinkDomain("homelab.page.link")
                .build()
        )
    }

    fun checkEmailVerified() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            if (it.isEmailVerified) {

            }
        }
    }

    fun resendEmail() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            verifyEmail(it)
        }

        Toast.makeText(
            context,
            getString(R.string.resend_verification_email_toast),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun emailVerificationLinkClicked(): Boolean = requireActivity().intent.extras != null

    private fun signInSuccessfully(result: FirebaseAuthUIAuthenticationResult): Boolean =
        result.resultCode == Activity.RESULT_OK

    private fun isUserLoggedIn(user: FirebaseUser?): Boolean = user != null

}