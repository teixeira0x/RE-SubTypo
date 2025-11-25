package com.teixeira0x.subtypo.core.ui.event

sealed class UiEvent {

    data class Toast(val message: String) : UiEvent()
}
