package com.teixeira0x.subtypo.core.ui.dialog

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R

fun Context.showConfirmDialog(
    title: Int,
    message: Int,
    cancelable: Boolean = true,
    onNegativeClick: (DialogInterface, Int) -> Unit = { _, _ -> },
    onPositiveClick: (DialogInterface, Int) -> Unit = { _, _ -> },
) {
    showConfirmDialog(
        title = getString(title),
        message = getString(message),
        cancelable = cancelable,
        onNegativeClick = onNegativeClick,
        onPositiveClick = onPositiveClick,
    )
}

fun Context.showConfirmDialog(
    title: String,
    message: String,
    cancelable: Boolean = true,
    onNegativeClick: (DialogInterface, Int) -> Unit = { _, _ -> },
    onPositiveClick: (DialogInterface, Int) -> Unit = { _, _ -> },
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(cancelable)
        .setNegativeButton(R.string.no, onNegativeClick)
        .setPositiveButton(R.string.yes, onPositiveClick)
        .show()
}
