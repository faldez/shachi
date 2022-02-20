package com.faldez.shachi.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class CustomDividerItemDecoration(
    context: Context,
    orientation: Int,
    private val bottomHeight: Int,
) :
    DividerItemDecoration(context, orientation) {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom: Int = top + drawable?.intrinsicHeight!!

            drawable?.bounds = Rect(left, top, right, bottom)
            drawable?.draw(c)

        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        val childCount = parent.adapter?.itemCount ?: 0
        if (childAdapterPosition == childCount - 1) {
            Log.d("CustomDividerItemDecoration", "set outRect.bottom = $bottomHeight")
            outRect.bottom = bottomHeight
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}