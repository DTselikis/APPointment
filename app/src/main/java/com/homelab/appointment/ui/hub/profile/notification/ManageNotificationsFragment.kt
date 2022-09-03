package com.homelab.appointment.ui.hub.profile.notification

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentManageNotificationsBinding
import com.homelab.appointment.listener.RecyclerViewItemSwipeListener
import kotlin.math.roundToInt


class ManageNotificationsFragment : BottomSheetDialogFragment() {

    enum class NotificationType(val code: Int) {
        CANCELLATION(0),
        CUSTOM(1)
    }

    enum class NotificationStatus(val code: Int) {
        SENT(0),
        READ(1)
    }

    private val args: ManageNotificationsFragmentArgs by navArgs()
    private val viewModel: ManageNotificationsViewModel by viewModels()

    private lateinit var binding: FragmentManageNotificationsBinding

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private lateinit var adapter: NotificationsAdapter

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

        adapter = NotificationsAdapter(this@ManageNotificationsFragment)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            notificationsRv.adapter = this@ManageNotificationsFragment.adapter
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
            peekHeight = 220.dp
            saveFlags = BottomSheetBehavior.SAVE_ALL
        }


        viewModel.fetchNotifications(args.uid)

        val deleteIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_delete_forever_30,
            requireActivity().theme
        )

        val swipeListener = RecyclerViewItemSwipeListener(
            RecyclerViewItemSwipeListener.SEPARATE_SWIPE_FROM_DRAG,
            RecyclerViewItemSwipeListener.SWIPE_BOTH_WAYS,
            deleteIcon!!,
            ColorDrawable(Color.RED),
        ) { position ->
            adapter.notifyItemRemoved(position)
            viewModel.deleteNotification(adapter.getItemAtPosition(position))
        }

        ItemTouchHelper(swipeListener).attachToRecyclerView(binding.notificationsRv)
        observeNotificationsForDisplay()
    }

    private fun observeNotificationsForDisplay() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.notificationsForDisplay.observe(viewLifecycleOwner) { notifications ->
                binding.apply {
                    if (notifications.isNotEmpty()) {
                        noNotificationsMsg.visibility = View.GONE
                        expandCollapseArrow.visibility = View.VISIBLE
                        notificationsRv.visibility = View.VISIBLE

                    } else {
                        noNotificationsMsg.visibility = View.VISIBLE
                        expandCollapseArrow.animate().rotation(180f)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.deleteOldNotifications(args.uid)
    }

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()
}