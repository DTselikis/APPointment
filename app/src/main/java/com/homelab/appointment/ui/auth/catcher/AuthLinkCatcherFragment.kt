package com.homelab.appointment.ui.auth.catcher

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.homelab.appointment.R
import com.homelab.appointment.data.LinkType
import kotlinx.coroutines.flow.collectLatest

class AuthLinkCatcherFragment : Fragment() {

    private val viewModel: AuthLinkCatcherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth_link_catcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeEmailUpdated()

        handleLink(requireActivity().intent.data!!)
    }

    private fun handleLink(uri: Uri) {
        val continueUrl = uri.getContinueUrl()
        val type = getLinkType(continueUrl)

        when (type) {
            LinkType.EMAIL_VERIFICATION ->
                if (isNewUser(continueUrl)) navigateToProfile()
                else navigateToHub()
            null -> {}
        }
    }

    private fun observeEmailUpdated() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.emailUpdated.collectLatest { updated ->
                if (updated) {
                    navigateToHub()
                }
            }
        }
    }

    private fun navigateToHub() {
        // TODO navigate to Hub
        Log.d("HIT", "navigateToHub: HIT")
    }

    private fun navigateToProfile() {
        findNavController().navigate(R.id.action_authLinkCatcherFragment_to_profileFragment)
    }

    private fun isNewUser(continueUrl: String): Boolean = continueUrl.contains("true")

    private fun getLinkType(link: String): LinkType? =
        if (link.contains("emailVerified")) LinkType.EMAIL_VERIFICATION
        else null

    private fun Uri.getContinueUrl(): String =
        this.getQueryParameter("continueUrl")!!

}