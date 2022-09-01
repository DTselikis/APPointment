package com.homelab.appointment.ui.hub.profile.notification

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.homelab.appointment.R
import com.homelab.appointment.databinding.FragmentManageNotificationsBinding
import kotlin.math.roundToInt


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

    private lateinit var swipeHelper: ItemTouchHelper

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
            peekHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                220f,
                resources.displayMetrics
            ).toInt()
            saveFlags = BottomSheetBehavior.SAVE_ALL
        }


        viewModel.fetchNotifications(args.uid)

        setupSwipeToDelete()
    }

    // Code from https://medium.com/getpowerplay/understanding-swipe-and-drag-gestures-in-recyclerview-cb3136beff20
    // https://github.com/ishanknijhawan/SwipeGestures
    // and for background color and icon https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
    private fun setupSwipeToDelete() {
        val height: Int
        val width: Int
        resources.displayMetrics.let { metrics ->
            height = (metrics.heightPixels / metrics.density).toInt().dp
            width = (metrics.widthPixels / metrics.density).toInt().dp
        }

        val deleteIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_delete_forever_30,
            requireActivity().theme
        )
        val deleteColor =
            ResourcesCompat.getColor(resources, R.color.email_red, requireActivity().theme)

        swipeHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val background = ColorDrawable(Color.RED)
                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconBottom = iconTop + deleteIcon.intrinsicHeight

                if (dX > 0) { // Swiping to the right
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.roundToInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                } else if (dX < 0) { // Swiping to the left
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicHeight
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        itemView.right + dX.roundToInt() - backgroundCornerOffset,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else { // View is unSwiped
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(canvas)
                deleteIcon.draw(canvas)
            }

        })

        swipeHelper.attachToRecyclerView(binding.notificationsRv)
    }

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()

    fun getColor(@ColorRes color: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(color, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), color)
        }
    }
}