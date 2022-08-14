package com.homelab.appointment.ui.hub.profile.emailDialog

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.homelab.appointment.R
import com.homelab.appointment.data.EMAIL_VERIFIED_NAV_KEY
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
            fragmentVerificationEmailDialog = this@VerificationEmailDialogFragment
        }

        observeEmailVerified()
    }

    fun closeFragment() {
        findNavController().navigateUp()
    }

    fun checkEmailVerified() {
        viewModel.checkEmailVerified(args.email)
    }

    private fun observeEmailVerified() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.emailVerified.collectLatest { verified ->
                if (verified) {
                    notifyEmailVerified()
                    closeFragment()
                } else {
                    setErrorState()
                }
            }
        }
    }

    private fun notifyEmailVerified() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            EMAIL_VERIFIED_NAV_KEY,
            true
        )
    }

    private fun setErrorState() {
        binding.apply {
            verificationDialogCard.strokeColor = getColor(R.color.error_stroke)
            verificationDialogCard.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.1f,
                resources.displayMetrics
            ).toInt()
            verificationDialogText.text = getString(R.string.verification_dialog_err_txt)
        }

    }

    private fun getColor(@ColorRes color: Int): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(color, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), color)
        }

}