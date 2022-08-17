package com.homelab.appointment.ui.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.homelab.appointment.R
import kotlinx.coroutines.flow.collectLatest

class StartupFragment : Fragment() {

    private val viewModel: StartupViewModel by viewModels()
    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_startup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userIsSignedIn()) {
                observeUserFetched()
                viewModel.fetchUser(firebaseUser!!.uid)
        } else {
            navigateToAuth()
        }
    }

    private fun observeUserFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userFetched.collectLatest { fetched ->
                if (fetched) {
                    if (firebaseUser!!.isEmailVerified) {
                        if (!isStoredAndAuthEmailsTheSame()) {
                            observeEmailUpdated()
                            viewModel.updateEmail(firebaseUser.email!!)
                        }
                    }
                    else {
                        sendVerificationEmail()
                        navigateToEmailVerification()
                    }
                }
            }
        }
    }

    private fun observeEmailUpdated() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.emailUpdated.collectLatest { updated ->
                if (updated) {
                    //TODO navigate to hub
                }
            }
        }
    }

    private fun sendVerificationEmail() {
        firebaseUser!!.sendEmailVerification(
            ActionCodeSettings.newBuilder()
                .setUrl("https://homelab.page.link/emailVerified?isNewUser=false")
                .setAndroidPackageName("com.homelab.appointment", true, null)
                .setHandleCodeInApp(true)
                .setDynamicLinkDomain("homelab.page.link")
                .build()
        )
    }

    private fun navigateToEmailVerification() {
        val action = StartupFragmentDirections.actionStartupFragmentToEmailVerificationFragment(viewModel.user)
        findNavController().navigate(action)
    }

    private fun navigateToAuth() {
        findNavController().navigate(R.id.action_startupFragment_to_authFragment)
    }

    private fun isStoredAndAuthEmailsTheSame(): Boolean =
        viewModel.user.email != firebaseUser!!.email

    private fun userIsSignedIn(): Boolean = firebaseUser == null
}