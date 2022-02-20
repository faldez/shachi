package com.faldez.shachi.widget

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyFooterDecoration(val height: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        val childCount = parent.adapter?.itemCount ?: 0
        if (childAdapterPosition == childCount - 1) {
            Log.d("EmptyFooterDecoration", "set outRect.bottom = $height")
            outRect.bottom = height
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}