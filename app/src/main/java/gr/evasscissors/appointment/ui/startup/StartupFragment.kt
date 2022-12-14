package gr.evasscissors.appointment.ui.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.databinding.FragmentStartupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StartupFragment : Fragment() {

    private val sharedViewModel: gr.evasscissors.appointment.NavViewModel by navGraphViewModels(R.id.auth_nav_graph)
    private val viewModel: StartupViewModel by viewModels()
    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    private lateinit var binding: FragmentStartupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_startup, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch(Dispatchers.Main) {
                delay(100)
                binding.businessLogo.animate().translationYBy(-650f).apply {
                    duration = 750
                    withEndAction {
                        binding.loadingIndicator.show()

                        isGooglePlayServicesAvailable()

                        if (userIsSignedIn()) {
                            observeUserFetched()
                            viewModel.fetchUser(firebaseUser!!.uid)
                        } else {
                            navigateToAuth()
                        }
                    }
                }
            }
        }
    }

    private fun observeUserFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userFetched.collectLatest { fetched ->
                if (fetched) {
                    sharedViewModel.user = viewModel.user

                    if (firebaseUser!!.isEmailVerified) {
                        if (!isStoredAndAuthEmailsTheSame()) {
                            observeEmailUpdated()
                            viewModel.updateEmail(firebaseUser.email!!)
                        } else {
                            navigateToBusinessInfo()
                        }
                    } else {
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
                    navigateToProfile()
                }
            }
        }
    }

    private fun sendVerificationEmail() {
        firebaseUser!!.sendEmailVerification(
            ActionCodeSettings.newBuilder()
                .setUrl("https://homelab.page.link/emailVerified?isNewUser=false")
                .setAndroidPackageName("gr.evasscissors.appointment", true, null)
                .setHandleCodeInApp(true)
                .setDynamicLinkDomain("homelab.page.link")
                .build()
        )
    }

    private fun navigateToEmailVerification() {
        val action =
            StartupFragmentDirections.actionStartupFragmentToEmailVerificationFragment(viewModel.user)
        findNavController().navigate(action)
    }

    private fun navigateToAuth() {
        findNavController().navigate(R.id.action_startupFragment_to_authFragment)
    }

    private fun navigateToBusinessInfo() {
        if (viewModel.user.activeNotifications!! > 0)
            activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
                ?.getOrCreateBadge(R.id.profileFragment)?.number =
                viewModel.user.activeNotifications!!

        findNavController().navigate(R.id.action_startupFragment_to_businessInfoFragment)
    }

    private fun navigateToProfile() {
        if (viewModel.user.activeNotifications!! > 0)
            activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
                ?.getOrCreateBadge(R.id.profileFragment)?.number =
                viewModel.user.activeNotifications!!

        findNavController().navigate(R.id.action_startupFragment_to_profileFragment)
    }

    private fun isStoredAndAuthEmailsTheSame(): Boolean =
        viewModel.user.email == firebaseUser!!.email

    private fun userIsSignedIn(): Boolean = firebaseUser != null

    private fun isGooglePlayServicesAvailable() {
        GoogleApiAvailability.getInstance().also {
            val status = it.isGooglePlayServicesAvailable(requireContext())
            if (status != ConnectionResult.SUCCESS) {
                if (it.isUserResolvableError(status)) {
                    it.getErrorDialog(requireParentFragment(), status, 1234) {}
                }
            }
        }
    }


}