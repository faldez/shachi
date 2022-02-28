package com.faldez.shachi

import android.app.Application
import com.google.android.material.color.DynamicColors

class ShachiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}