package com.homelab.appointment.ui.hub.profile.notification

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentManageNotificationsBinding


class ManageNotificationsFragment : BottomSheetDialogFragment() {

    enum class NotificationType(val code: Int) {
        CANCELLATION(0),
        CUSTOM(1)
    }

    private val args: ManageNotificationsFragmentArgs by navArgs()
    private val viewModel: ManageNotificationsViewModel by viewModels()

    private lateinit var binding: FragmentManageNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_notifications, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ManageNotificationsFragment.viewModel
        }

        viewModel.fetchNotifications(args.uid)
    }

    fun getColor(@ColorRes color: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(color, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), color)
        }
    }
}