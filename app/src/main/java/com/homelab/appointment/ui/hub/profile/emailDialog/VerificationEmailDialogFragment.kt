package com.homelab.appointment.ui.hub.profile.emailDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentVerificationEmailDialogBinding
import kotlinx.coroutines.flow.collectLatest

class VerificationEmailDialogFragment : DialogFragment() {
    private val args: VerificationEmailDialogFragmentArgs by navArgs<VerificationEmailDialogFragmentArgs>()
    private val viewModel: VerificationEmailDialogViewModel by viewModels()

    private lateinit var binding: FragmentVerificationEmailDialogBinding

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
//        AlertDialog.Builder(requireContext())
//            .setTitle("Verification sent")
//            .setMessage("Verification email sent to")
//            .setPositiveButton("Verified"){_, _ ->}
//            .setNegativeButton("Cancel"){_, _ ->}
//            .show()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_verification_email_dialog,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            email = args.email
        }

        observeEmailVerified()
    }

    private fun observeEmailVerified() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.emailVerified.collectLatest { verified ->
                if (verified) {
                    findNavController().navigateUp()
                }
            }
        }
    }
}