package com.homelab.appointment.ui.hub.profile.notification

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetBehavior = bottomSheetDialog.behavior

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_manage_notifications,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            notificationsRv.adapter = NotificationsAdapter(this@ManageNotificationsFragment)
            viewModel = this@ManageNotificationsFragment.viewModel
            // Set initial behaviour because listener not triggers when sheet is opening
            expandCollapseArrow.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        bottomSheetBehavior.apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> binding.expandCollapseArrow.setOnClickListener {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> binding.expandCollapseArrow.setOnClickListener {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.expandCollapseArrow.rotation = slideOffset * 180
                }
            })
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