package com.teixeira0x.subtypo.ui

import android.content.Context
import android.content.Intent
import com.teixeira0x.subtypo.ui.about.activity.AboutActivity

/** Navigate to about activity. */
fun navigateToAboutActivity(context: Context) {
    context.startActivity(Intent(context, AboutActivity::class.java))
}
