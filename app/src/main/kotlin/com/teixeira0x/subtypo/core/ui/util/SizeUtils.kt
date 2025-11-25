package com.teixeira0x.subtypo.core.ui.util

import android.content.res.Resources

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
