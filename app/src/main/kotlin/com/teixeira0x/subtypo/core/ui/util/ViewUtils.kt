/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.core.ui.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

val Context.layoutInflater: LayoutInflater
    get() = if (this is Activity) this.layoutInflater else LayoutInflater.from(this)

inline fun View.applySystemBarsInsets(
    consume: Boolean = false,
    crossinline block: (Insets) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        block(systemBars)

        if (consume) {
            WindowInsetsCompat.CONSUMED
        } else {
            insets
        }
    }
}
