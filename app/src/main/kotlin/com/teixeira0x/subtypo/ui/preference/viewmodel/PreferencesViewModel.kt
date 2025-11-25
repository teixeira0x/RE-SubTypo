package com.teixeira0x.subtypo.ui.preference.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.teixeira0x.subtypo.R

class PreferencesViewModel : ViewModel() {

    private val _screenHistory = MutableLiveData<List<Int>>(listOf(R.xml.preferences))
    val screenHistory: LiveData<List<Int>> = _screenHistory

    val currentScreenId: LiveData<Int> = _screenHistory.map { it.last() }

    fun navigateToScreen(id: Int) {
        _screenHistory.value = _screenHistory.value!!.plus(id)
    }

    fun navigateBack() {
        val newHistory = _screenHistory.value!!.dropLast(1)
        if (newHistory.isNotEmpty()) {
            _screenHistory.value = newHistory
        }
    }
}
