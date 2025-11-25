package com.teixeira0x.subtypo.core.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Class to manage preferences, as I will use this class a lot I decided to turn it into an 'object'
 * as it is easier than always injecting it.
 *
 * @author Felipe Teixeira
 */
object PreferencesManager {

    // General
    const val KEY_GENERAL = "pref_configure_general_key"
    const val KEY_VIDEO_PLAYER = "pref_configure_video_player_key"
    const val KEY_APPEARANCE_UI_MODE = "pref_appearance_ui_mode_key"
    const val KEY_APPEARANCE_DYNAMICCOLORS = "pref_appearance_dynamiccolors_key"
    const val KEY_APPEARANCE_AMOLED = "pref_appearance_amoled_key"
    const val KEY_CAPTION_STYLE = "pref_caption_style"

    // About
    const val KEY_ABOUT_APP = "pref_about_app"

    // others
    const val KEY_SHOW_UPDATE_INFO = "pref_show_update_info"

    lateinit var preferences: SharedPreferences
        private set

    /* Initialize in the application class */
    fun init(context: Context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        this.preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        this.preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    val appearanceUIMode: Int
        get() =
            when (preferences.getInt(KEY_APPEARANCE_UI_MODE, 0)) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

    var appearanceDynamicColors: Boolean
        get() = preferences.getBoolean(KEY_APPEARANCE_DYNAMICCOLORS, true)
        set(value) = preferences.edit { putBoolean(KEY_APPEARANCE_DYNAMICCOLORS, value) }

    var appearanceAmoled: Boolean
        get() = preferences.getBoolean(KEY_APPEARANCE_AMOLED, false)
        set(value) = preferences.edit { putBoolean(KEY_APPEARANCE_AMOLED, value) }

    // Others
    var otherShowUpdateInfo: Boolean
        get() = preferences.getBoolean(KEY_SHOW_UPDATE_INFO, true)
        set(value) = preferences.edit { putBoolean(KEY_SHOW_UPDATE_INFO, value) }
}
