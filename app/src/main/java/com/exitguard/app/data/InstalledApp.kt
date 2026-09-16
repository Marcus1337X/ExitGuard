package com.exitguard.app.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val iconBitmap: Bitmap? = null
)
