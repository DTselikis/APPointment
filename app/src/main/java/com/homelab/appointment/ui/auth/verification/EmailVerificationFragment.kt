package com.homelab.appointment.ui.auth.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentEmailVerificationBinding
import kotlinx.coroutines.flow.collectLatest

class EmailVerificationFragment : Fragment() {
    private val args: EmailVerificationFragmentArgs by navArgs<EmailVerificationFragmentArgs>()
    private val viewModel: EmailVerificationViewModel by viewModels()

    private lateinit var binding: FragmentEmailVerificationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_email_verification,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            emailVerificationText.text = args.user.email
            emailVerificationFragment = this@EmailVerificationFragment
        }

        observeEmailVerified()
    }

    fun checkEmailVerified() {
        viewModel.checkEmailVerified()
    }

    private fun observeEmailVerified() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.emailVerified.collectLatest { verified ->
                if (verified) {
                    // TODO navigate to hub
                } else {
                    Snackbar.make(
                        binding.emailVerificationIcon,
                        getString(R.string.verification_dialog_err_txt),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
}