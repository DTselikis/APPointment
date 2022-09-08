package com.homelab.appointment.ui.auth.reauth

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.homelab.appointment.R
import com.homelab.appointment.data.RE_AUTH_NAV_KEY
import com.homelab.appointment.databinding.FragmentReAuthBinding
import kotlinx.coroutines.flow.collectLatest

class ReAuthFragment : BottomSheetDialogFragment() {
    private val args: ReAuthFragmentArgs by navArgs()
    private val viewModel: ReAuthViewModel by viewModels()

    private lateinit var binding: FragmentReAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_re_auth, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ReAuthFragment.viewModel
            fragmentReAuth = this@ReAuthFragment
        }

        observeAuthentication()
    }

    private fun closeFragment() {
        notifyReAuthFinished()
        binding.reAuthProgress.hide()
        findNavController().navigateUp()
    }

    private fun observeAuthentication() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.authenticated.collectLatest { authenticated ->
                if (authenticated) {
                    closeFragment()
                } else {
                    showFailureMessage()
                    binding.reAuthBtn.isEnabled = true
                }
            }
        }
    }

    private fun notifyReAuthFinished() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(RE_AUTH_NAV_KEY, true)
    }

    private fun showFailureMessage() {
        val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.email_red, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), R.color.email_red)
        }

        Snackbar.make(
            requireContext(),
            binding.reAuthBtn,
            getString(R.string.authentication_failed),
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(color)
            .show()
    }

    fun reAuthenticate() {
        binding.reAuthProgress.show()
        binding.reAuthBtn.isEnabled = false
        viewModel.reAuthenticate(args.email)
    }
}