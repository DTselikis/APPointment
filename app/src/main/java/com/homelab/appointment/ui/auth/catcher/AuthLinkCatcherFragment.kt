package com.homelab.appointment.ui.auth.catcher

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.homelab.appointment.R
import com.homelab.appointment.data.LinkType

class AuthLinkCatcherFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth_link_catcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun navigateToHub() {
        // TODO navigate to Hub
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