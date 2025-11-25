package com.teixeira0x.subtypo.core.ui.util

import android.app.Activity
import android.content.res.Configuration
import com.google.android.material.color.DynamicColors
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.preference.PreferencesManager

fun Activity.updateTheme() {
    if (PreferencesManager.appearanceDynamicColors) {
        DynamicColors.applyToActivityIfAvailable(this)
    }

    updateThemeStyle()
}

fun Activity.updateThemeStyle() {
    if (PreferencesManager.appearanceAmoled && resources.configuration.isDarkMode) {
        theme.applyStyle(R.style.Amoled, true)
    }
}

val Configuration.isDarkMode: Boolean
    get() = uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
