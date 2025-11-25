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

package com.teixeira0x.subtypo.core.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.databinding.FragmentProgressDialogBinding

class ProgressDialogFragment : DialogFragment() {

    companion object {
        private const val KEY_ORIENTATION_ARG = "key_orientationArg"
        private const val KEY_STYLE_ARG = "key_styleArg"
        private const val KEY_CANCELABLE_ARG = "key_styleArg"
        private const val KEY_MESSAGE_ARG = "key_messageArg"

        const val ORIENTATION_VERTICAL = 0
        const val ORIENTATION_HORIZONTAL = 1

        const val STYLE_DEFAULT = 0
        const val STYLE_NO_BACKGROUND = 1

        @JvmStatic
        fun newInstance(
            orientation: Int = ORIENTATION_VERTICAL,
            style: Int = STYLE_DEFAULT,
            cancelable: Boolean = false,
            message: String? = null,
        ): ProgressDialogFragment {
            return ProgressDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putInt(KEY_ORIENTATION_ARG, orientation)
                        putInt(KEY_STYLE_ARG, style)
                        putBoolean(KEY_CANCELABLE_ARG, cancelable)
                        putString(KEY_MESSAGE_ARG, message)
                    }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentProgressDialogBinding.inflate(layoutInflater)

        val arguments = requireArguments()
        val orientation = arguments.getInt(KEY_ORIENTATION_ARG)
        val style = arguments.getInt(KEY_STYLE_ARG)
        val cancelable = arguments.getBoolean(KEY_CANCELABLE_ARG)
        val message = arguments.getString(KEY_MESSAGE_ARG)

        if (message != null) {
            binding.messageSide.text = message
            binding.messageBelow.text = message

            binding.messageSide.isVisible = orientation == ORIENTATION_HORIZONTAL
            binding.messageBelow.isVisible = orientation == ORIENTATION_VERTICAL
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(binding.root)

        val dialog = builder.create()
        if (style == STYLE_NO_BACKGROUND) {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        setCancelable(cancelable)

        return dialog
    }
}
