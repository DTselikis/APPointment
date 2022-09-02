package com.homelab.appointment.listener

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class RecyclerViewItemSwipeListener(
    policy: Int = SEPARATE_SWIPE_FROM_DRAG,
    swipeDirection: Int = SWIPE_BOTH_WAYS,
    private val icon: Drawable,
    private val backgroundColor: ColorDrawable = ColorDrawable(Color.RED),
    private val onSwipe: () -> Unit
) : ItemTouchHelper.SimpleCallback(policy, swipeDirection) {
    companion object {
        const val SEPARATE_SWIPE_FROM_DRAG = 0
        const val SWIPE_BOTH_WAYS = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwipe.invoke()
    }

    // Code from https://medium.com/getpowerplay/understanding-swipe-and-drag-gestures-in-recyclerview-cb3136beff20
    // https://github.com/ishanknijhawan/SwipeGestures
    // and for background color and icon https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
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

        val itemView = viewHolder.itemView
        val backgroundColorCornerOffset = 20
        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        val (iconLeft, iconRight) = if (dX > 0) { // Swiping to the right
            backgroundColor.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.roundToInt() + backgroundColorCornerOffset,
                itemView.bottom
            )

            Pair(itemView.left + iconMargin, itemView.left + iconMargin + icon.intrinsicWidth)
        } else if (dX < 0) { // Swiping to the left
            backgroundColor.setBounds(
                itemView.right + dX.roundToInt() - backgroundColorCornerOffset,
                itemView.top,
                itemView.right,
                itemView.bottom
            )

            Pair(itemView.right - iconMargin - icon.intrinsicHeight, itemView.right - iconMargin)
        } else { // View is unSwiped
            backgroundColor.setBounds(0, 0, 0, 0)
            Pair(0, 0)
        }

        backgroundColor.draw(canvas)
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(canvas)
    }
}