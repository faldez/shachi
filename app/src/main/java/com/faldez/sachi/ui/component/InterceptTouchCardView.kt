package com.faldez.sachi.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.card.MaterialCardView

class InterceptTouchMaterialCardView(context: Context, attr: AttributeSet) :
    MaterialCardView(context, attr) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
}