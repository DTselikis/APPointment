package com.homelab.appointment.ui.hub.profile.emailDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentVerificationEmailDialogBinding

class VerificationEmailDialogFragment : DialogFragment() {
    private val args: VerificationEmailDialogFragmentArgs by navArgs<VerificationEmailDialogFragmentArgs>()
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
    }
}