package com.monir.preciseattendance.ui.location

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwraps themed or wrapped contexts to the hosting [Activity], or null if there is none. */
internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
