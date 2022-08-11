package com.homelab.appointment.ui.hub.profile

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.homelab.appointment.R
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

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            lifecycleScope.launch {
                try {
                    val compressedFile = Compressor.compress(requireContext(), uri?.toFile()!!)
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
        }

        observePicUploaded()
    }

    fun pickImage() {
        openGallery.launch("image/*")
    }

    private fun observePicUploaded() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.picUploaded.collectLatest { stored ->
                val (text, color) = getTextAndColor(stored)

                Snackbar.make(binding.fbEdit, text, Snackbar.LENGTH_LONG)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(color)
                    .show()
            }
        }
    }

    private fun getTextAndColor(stored: Boolean): Pair<String, Int> {
        val text: String
        val color: Int
        when (stored) {
            true -> {
                text = getString(R.string.profile_pic_saved)
                color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.teal_200, requireActivity().theme)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.teal_200)
                }
            }
            false -> {
                text = getString(R.string.profile_pic__not_saved)
                color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.email_red, requireActivity().theme)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.email_red)
                }
            }
        }

        return Pair(text, color)
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