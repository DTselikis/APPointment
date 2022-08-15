package com.homelab.appointment.ui.hub.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.homelab.appointment.R
import com.homelab.appointment.data.RE_AUTH_NAV_KEY
import com.homelab.appointment.databinding.FragmentProfileBinding
import com.homelab.appointment.ui.hub.HubSharedViewModel
import id.zelory.compressor.Compressor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private val sharedViewModel: HubSharedViewModel by activityViewModels()
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(sharedViewModel.user)
    }

    private lateinit var binding: FragmentProfileBinding
    private lateinit var photoUri: Uri

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            lifecycleScope.launch {
                try {
                    val compressedFile = Compressor.compress(requireContext(), uri?.toFile()!!)
                    photoUri = uri
                    viewModel.storeImageToFirebase(compressedFile)
                } catch (e: NoSuchFileException) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.compress_img_err),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, null, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ProfileFragment.viewModel
            profileFragment = this@ProfileFragment

            emailEditText.apply {
                doOnTextChanged { text, _, _, _ ->
                    if (text.toString() != this@ProfileFragment.viewModel.user.email) {
                        showSaveBtn(emailEdit, fbEdit)
                    } else if (text.toString() == this@ProfileFragment.viewModel.user.email) {
                        hideSaveBtn(fbEdit, emailEdit)
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        saveEditsBtn.setOnClickListener {
                            this@ProfileFragment.viewModel.verifyNewEmail()
                        }
                    }
                }
            }

            phoneEditText.apply {
                doOnTextChanged { text, _, _, _ ->
                    val currentPhone = this@ProfileFragment.viewModel.user.phone
                    if (text.toString() != currentPhone) {
                        showSaveBtn(photoCard, emailEdit)
                    } else if (text.toString() == currentPhone) {
                        hideSaveBtn(emailEdit, photoCard)
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        saveEditsBtn.setOnClickListener {
                            this@ProfileFragment.viewModel.storeUpdatedPhone()
                        }
                    }
                }
            }
        }

        observePicUploaded()
        observeVerificationEmailSent()
        observeReAuthFinished()
        observeReAuthRequirement()
        observeUpdatedPhoneStored()
    }

    fun pickImage() {
        openGallery.launch("image/*")
    }

    private fun observeReAuthFinished() {
        findNavController().previousBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Boolean>(RE_AUTH_NAV_KEY)
                .observe(viewLifecycleOwner) { reAuthFinished ->
                    if (reAuthFinished) viewModel.verifyNewEmail()
                    savedStateHandle.remove<Boolean>(RE_AUTH_NAV_KEY)
                }
        }
    }

    private fun observeReAuthRequirement() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.needsReAuth.collectLatest { needsReAuth ->
                if (needsReAuth) {
                    val action =
                        ProfileFragmentDirections.actionProfileFragmentToReAuthFragment(
                            sharedViewModel.user.email!!
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun observeVerificationEmailSent() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.verificationEmailSent.collectLatest { sent ->
                if (sent) {
                    informUser()
                }
            }
        }
    }

    private fun informUser() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.verification_dialog_title))
                .setMessage(getString(R.string.verification_dialog_verify_email, viewModel.email.value))
                .setOnDismissListener {
                    FirebaseAuth.getInstance().signOut()
                    AuthUI.getInstance().signOut(requireContext())
                    findNavController().navigate(R.id.action_profileFragment_to_authFragment)
                }
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun observeUpdatedPhoneStored() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.updatedPhoneStored.collectLatest { stored ->
                val (text, color) = when (stored) {
                    true -> Pair(getString(R.string.phone_updated), R.color.teal_200)
                    else -> Pair(getString(R.string.email_not_updated), R.color.email_red)
                }

                showSnackBar(text, color)
            }
        }
    }

    private fun observePicUploaded() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.picUploaded.collectLatest { stored ->
                val (text, color) = when (stored) {
                    true -> Pair(getString(R.string.profile_pic_saved), getColor(R.color.teal_200))
                    false -> Pair(
                        getString(R.string.profile_pic__not_saved),
                        getColor(R.color.email_red)
                    )
                }

                Snackbar.make(binding.fbEdit, text, Snackbar.LENGTH_LONG)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(color)
                    .show()

                viewModel.updateProfilePic(photoUri.toString())
            }
        }
    }

    private fun showSnackBar(message: String, @ColorRes color: Int) {
        Snackbar.make(binding.saveEditsBtn, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(color))
            .show()
    }

    private fun showSaveBtn(top: View, below: View?) {
        binding.saveEditsBtn.apply {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = top.id
            }
            visibility = View.VISIBLE
        }

        below?.let {
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = binding.saveEditsBtn.id
            }
        }
    }

    private fun hideSaveBtn(below: View, top: View) {
        below.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = top.id
        }
        binding.saveEditsBtn.visibility = View.GONE
    }

    private fun getColor(@ColorRes color: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(color, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), color)
        }
    }

    private fun Uri.toFile(): File? {
        requireActivity().contentResolver.openInputStream(this)?.let { inputSteam ->
            val tmpFile = File.createTempFile("profile", "pic")
            val fileOutputStream = FileOutputStream(tmpFile)

            inputSteam.copyTo(fileOutputStream)
            inputSteam.close()
            fileOutputStream.close()

            return tmpFile
        }

        return null
    }

}