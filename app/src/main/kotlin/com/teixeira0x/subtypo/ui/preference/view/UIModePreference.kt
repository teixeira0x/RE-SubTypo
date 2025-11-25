package com.teixeira0x.subtypo.ui.preference.view

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R

class UIModePreference : Preference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        super.onClick()

        val items =
            arrayOf(
                getString(R.string.pref_appearance_ui_mode_value_follow),
                getString(R.string.pref_appearance_ui_mode_value_light),
                getString(R.string.pref_appearance_ui_mode_value_dark),
            )

        val prefs = sharedPreferences ?: return
        val editor = prefs.edit()
        val selectedItemIndex = prefs.getInt(key, 0)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, selectedItemIndex) { _, w -> editor.putInt(key, w) }
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> editor.apply() }
            .show()
    }

    private fun getString(resId: Int): String {
        return context.getString(resId)
    }
}
