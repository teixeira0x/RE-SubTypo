package com.teixeira0x.subtypo.ui.preference.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.CaptioningManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.DynamicColors
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.preference.PreferencesManager
import com.teixeira0x.subtypo.core.ui.base.Selectable
import com.teixeira0x.subtypo.core.ui.util.isDarkMode
import com.teixeira0x.subtypo.ui.navigateToAboutActivity
import com.teixeira0x.subtypo.ui.preference.viewmodel.PreferencesViewModel

class PreferencesFragment :
    PreferenceFragmentCompat(), OnSharedPreferenceChangeListener, Selectable {

    companion object {
        private const val RECREATE_ACTION_DELAY = 150L
    }

    private val viewModel by viewModels<PreferencesViewModel>()

    private val onBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = viewModel.navigateBack()
        }

    private val handler = Handler(Looper.getMainLooper())
    private val recreateAction = Runnable { activity?.recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.registerOnSharedPreferenceChangeListener(this)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferencesManager.unregisterOnSharedPreferenceChangeListener(this)
        onBackPressedCallback.isEnabled = false
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.currentScreenId.observe(this) { screenId ->
            onBackPressedCallback.isEnabled = screenId != R.xml.preferences
            setPreferencesFromResource(screenId, null)
            onScreenIdChange(screenId)
        }
    }

    private fun onScreenIdChange(screenId: Int) {
        when (screenId) {
            R.xml.preferences -> setupMainPreferences()
            R.xml.preferences_general -> setupGeneralPreferences()
            R.xml.preferences_player -> setupPlayerPreferences()
        }
    }

    private fun setupMainPreferences() {
        findPreference<Preference>(PreferencesManager.KEY_GENERAL)
            ?.setOnPreferenceClickListener {
                viewModel.navigateToScreen(R.xml.preferences_general)
                true
            }

        findPreference<Preference>(PreferencesManager.KEY_VIDEO_PLAYER)
            ?.setOnPreferenceClickListener {
                viewModel.navigateToScreen(R.xml.preferences_player)
                true
            }

        findPreference<Preference>(PreferencesManager.KEY_ABOUT_APP)
            ?.setOnPreferenceClickListener {
                navigateToAboutActivity(requireContext())
                true
            }
    }

    private fun setupGeneralPreferences() {
        findPreference<Preference>(PreferencesManager.KEY_APPEARANCE_DYNAMICCOLORS)?.apply {
            if (!DynamicColors.isDynamicColorAvailable()) {
                if (PreferencesManager.appearanceDynamicColors) {
                    PreferencesManager.appearanceDynamicColors = false
                }
                isEnabled = false
            }
        }

        findPreference<Preference>(PreferencesManager.KEY_APPEARANCE_AMOLED)?.apply {
            isEnabled = resources.configuration.isDarkMode
        }
    }

    private fun setupPlayerPreferences() {
        findPreference<Preference>(PreferencesManager.KEY_CAPTION_STYLE)?.setOnPreferenceClickListener {
            val intent = Intent(android.provider.Settings.ACTION_CAPTIONING_SETTINGS)
            startActivity(intent)
            true
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        when (key) {
            PreferencesManager.KEY_APPEARANCE_UI_MODE ->
                AppCompatDelegate.setDefaultNightMode(PreferencesManager.appearanceUIMode)
            PreferencesManager.KEY_APPEARANCE_DYNAMICCOLORS,
            PreferencesManager.KEY_APPEARANCE_AMOLED -> {
                handler.removeCallbacks(recreateAction)
                handler.postDelayed(recreateAction, RECREATE_ACTION_DELAY)
            }
        }
    }

    override fun onSelect() {
        onBackPressedCallback.isEnabled = viewModel.currentScreenId.value != R.xml.preferences
    }

    override fun onDeselect() {
        onBackPressedCallback.isEnabled = false
    }
}