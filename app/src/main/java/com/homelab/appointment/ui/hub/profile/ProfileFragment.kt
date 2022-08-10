package com.homelab.appointment.ui.hub.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentProfileBinding
import com.homelab.appointment.ui.hub.HubSharedViewModel

class ProfileFragment : Fragment() {

    private val sharedViewModel: HubSharedViewModel by activityViewModels()
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(sharedViewModel.user)
    }

    private lateinit var binding: FragmentProfileBinding

    private val openGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

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
    }

    fun pickImage() {
        openGallery.launch("image/*")
    }

}