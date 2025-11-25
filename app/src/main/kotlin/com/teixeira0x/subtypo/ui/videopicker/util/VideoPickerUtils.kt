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

package com.teixeira0x.subtypo.ui.videopicker.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.teixeira0x.subtypo.core.preference.PreferencesManager

private const val KEY_VIDEO_LIST_SORT_BY_NAME = "key_video_picker_sort"

private val preferences: SharedPreferences
    get() = PreferencesManager.preferences

var videoListSortByName: Boolean
    get() = preferences.getBoolean(KEY_VIDEO_LIST_SORT_BY_NAME, false)
    set(value) = preferences.edit { putBoolean(KEY_VIDEO_LIST_SORT_BY_NAME, value) }
