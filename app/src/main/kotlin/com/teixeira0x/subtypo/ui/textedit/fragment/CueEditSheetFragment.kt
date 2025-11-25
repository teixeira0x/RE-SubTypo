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

package com.teixeira0x.subtypo.ui.textedit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getMilliseconds
import com.teixeira0x.subtypo.core.ui.base.BaseBottomSheetFragment
import com.teixeira0x.subtypo.core.ui.dialog.showConfirmDialog
import com.teixeira0x.subtypo.core.ui.fragment.ProgressDialogFragment
import com.teixeira0x.subtypo.core.ui.util.showToastShort
import com.teixeira0x.subtypo.core.ui.validate.ValidationResult
import com.teixeira0x.subtypo.databinding.FragmentSheetCueEditBinding
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditIntent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditUiEvent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditUiState
import com.teixeira0x.subtypo.ui.textedit.util.CueFieldType
import com.teixeira0x.subtypo.ui.textedit.util.createTimeChip
import com.teixeira0x.subtypo.ui.textedit.util.decreaseTime
import com.teixeira0x.subtypo.ui.textedit.util.increaseTime
import com.teixeira0x.subtypo.ui.textedit.viewmodel.CueEditViewModel
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListIntent
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CueEditSheetFragment : BaseBottomSheetFragment() {

    companion object {
        private const val KEY_CUE_INDEX_ARG = "key_cueIndexArg"
        private const val KEY_VIDEO_POSITION_ARG = "key_videoPositionArg"

        @JvmStatic
        fun newInstance(
            playerPosition: Long = 0L,
            cueIndex: Int = -1,
        ): CueEditSheetFragment {
            return CueEditSheetFragment().apply {
                arguments =
                    Bundle().apply {
                        putLong(KEY_VIDEO_POSITION_ARG, playerPosition)
                        putInt(KEY_CUE_INDEX_ARG, cueIndex)
                    }
            }
        }
    }

    private val cueListViewModel by activityViewModels<CueListViewModel>()
    private val viewModel by viewModels<CueEditViewModel>()
    private var progress: ProgressDialogFragment? = null
    private var _binding: FragmentSheetCueEditBinding? = null
    private val binding: FragmentSheetCueEditBinding
        get() = checkNotNull(_binding) { "CueEditSheetFragment has been destroyed" }

    private var playerPosition: Long = 0L
    private var cueIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            playerPosition = it.getLong(KEY_VIDEO_POSITION_ARG)
            cueIndex = it.getInt(KEY_CUE_INDEX_ARG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSheetCueEditBinding.inflate(inflater).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        configureUI()

        viewModel.doIntent(CueEditIntent.LoadCue(cueListViewModel.subtitle, cueIndex = cueIndex))
    }

    override fun onStart() {
        super.onStart()
        validateAllFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.cueEditUiState.observe(this) { state ->
            when (state) {
                is CueEditUiState.Loading -> onLoadingChange(true)
                is CueEditUiState.Loaded -> {
                    onLoadingChange(false)
                    onCueLoaded(state)
                }

                is CueEditUiState.Error -> {
                    requireContext().showToastShort(state.message)
                    dismissProgress()
                    dismiss()
                }
            }
        }

        viewModel.customUiEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is CueEditUiEvent.CueInserted ->
                        cueListViewModel.doIntent(CueListIntent.ScrollTo(event.index))

                    is CueEditUiEvent.CueUpdated ->
                        cueListViewModel.doIntent(CueListIntent.ScrollTo(event.index))

                    is CueEditUiEvent.ShowProgress -> showProgress(event.message)
                    is CueEditUiEvent.DismissProgress -> dismissProgress()
                    is CueEditUiEvent.Dismiss -> {
                        dismissProgress()
                        dismiss()
                    }

                    is CueEditUiEvent.UpdateField -> updateField(event)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onCueLoaded(state: CueEditUiState.Loaded) {
        binding.apply {
            val cue = state.cue
            if (cue != null) {
                toolbar.title = getString(R.string.subtitle_cue_edit)
                tieStartTime.setText(cue.startTime.getFormattedTime(viewModel.subtitleTimeFormat))
                tieEndTime.setText(cue.endTime.getFormattedTime(viewModel.subtitleTimeFormat))
                tieText.setText(cue.text)
            } else {
                toolbar.title = getString(R.string.subtitle_cue_add)
                tieStartTime.setText(playerPosition.getFormattedTime(viewModel.subtitleTimeFormat))
                tieEndTime.setText(
                    (playerPosition + 2000).getFormattedTime(viewModel.subtitleTimeFormat)
                )
            }
        }
        configureToolbar()
        validateAllFields()
    }

    private fun onLoadingChange(isLoading: Boolean) {
        setCancelable(!isLoading)
        binding.apply {
            tieStartTime.isEnabled = !isLoading
            tieEndTime.isEnabled = !isLoading
            tieText.isEnabled = !isLoading
            dialogButtons.cancel.isEnabled = !isLoading
            dialogButtons.save.isEnabled = !isLoading
        }
    }

    private fun showProgress(message: Int) {
        progress?.dismiss()
        progress =
            ProgressDialogFragment.newInstance(
                orientation = ProgressDialogFragment.ORIENTATION_HORIZONTAL,
                style = ProgressDialogFragment.STYLE_DEFAULT,
                message = getString(message),
            )
        progress?.show(childFragmentManager, null)
    }

    private fun dismissProgress() {
        progress?.dismiss()
        progress = null
    }

    private fun configureUI() {
        configureListeners()
        configureTextWatchers()
        configureChips()
    }

    private fun configureListeners() {
        binding.apply {
            dialogButtons.cancel.setOnClickListener { dismiss() }
            dialogButtons.save.setOnClickListener {
                if (!isValidFields()) {
                    return@setOnClickListener
                }

                val startTime = binding.tieStartTime.text.toString().getMilliseconds()
                val endTime = binding.tieEndTime.text.toString().getMilliseconds()
                val text = binding.tieText.text.toString().trim()

                val subtitle = cueListViewModel.subtitle

                val cues = subtitle.data.cues.toMutableList()
                val index =
                    cues
                        .indexOfLast { playerPosition >= it.startTime }
                        .takeIf { it > -1 }
                        ?.plus(1) ?: 0

                if (cueIndex >= 0) {

                    cues[cueIndex] = Cue(
                        startTime = startTime,
                        endTime = endTime,
                        text = text
                    )

                    cueListViewModel.doIntent(CueListIntent.ScrollTo(cueIndex))
                } else {
                    cues.add(
                        index,
                        Cue(
                            startTime = startTime,
                            endTime = endTime,
                            text = text
                        ),
                    )

                    cueListViewModel.doIntent(CueListIntent.ScrollTo(index))
                }

                val data = subtitle.data.copy(cues = cues)

                cueListViewModel.doIntent(
                    CueListIntent.LoadSubtitle(
                        subtitle.copy(data = data)
                    )
                )

                dismiss()
            }

        }
    }

    private fun configureToolbar() {
        binding.toolbar.menu.clear()
        binding.toolbar.menu
            .add(R.string.cue_remove)
            .setIcon(R.drawable.ic_delete)
            .setEnabled(cueIndex >= 0)
            .setOnMenuItemClickListener {
                requireContext().showConfirmDialog(
                    title = R.string.remove,
                    message = R.string.subtitle_cue_remove_msg,
                ) { _, _ ->
                    // Remove cue logic
                }
                true
            }
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    private fun configureTextWatchers() {
        binding.tieStartTime.doAfterTextChanged { text ->
            viewModel.doIntent(
                CueEditIntent.ValidateCueField(
                    type = CueFieldType.START_TIME,
                    text = text.toString(),
                )
            )
        }

        binding.tieEndTime.doAfterTextChanged { text ->
            viewModel.doIntent(
                CueEditIntent.ValidateCueField(type = CueFieldType.END_TIME, text = text.toString())
            )
        }

        binding.tieText.doAfterTextChanged { text ->
            viewModel.doIntent(
                CueEditIntent.ValidateCueField(type = CueFieldType.TEXT, text = text.toString())
            )
        }
    }

    private fun configureChips() {
        val chipTimes = arrayOf(1, 5, 10, 15)

        chipTimes.forEach { time ->
            val chipDecrease =
                createTimeChip(requireContext(), "-${time}") {
                    onTimeChipClick(false, time.toLong())
                }
            val chipIncrease =
                createTimeChip(requireContext(), "+${time}") {
                    onTimeChipClick(true, time.toLong())
                }
            binding.chipGroupTime.addView(chipDecrease)
            binding.chipGroupTime.addView(chipIncrease)
        }
    }

    private fun onTimeChipClick(isIncrease: Boolean, millis: Long) {
        val focusedField =
            when {
                binding.tieStartTime.isFocused -> binding.tieStartTime
                binding.tieEndTime.isFocused -> binding.tieEndTime
                else -> null
            }

        focusedField?.apply {
            val savedSelection = selectionStart
            val updatedTime =
                if (isIncrease) {
                    text.toString().increaseTime(millis, viewModel.subtitleTimeFormat)
                } else {
                    text.toString().decreaseTime(millis, viewModel.subtitleTimeFormat)
                }
            setText(updatedTime)
            setSelection(savedSelection)
        }
    }

    private fun validateAllFields() {
        viewModel.doIntent(
            CueEditIntent.ValidateCueField(
                type = CueFieldType.START_TIME,
                text = binding.tieStartTime.text.toString(),
            )
        )

        viewModel.doIntent(
            CueEditIntent.ValidateCueField(
                type = CueFieldType.END_TIME,
                text = binding.tieEndTime.text.toString(),
            )
        )

        viewModel.doIntent(
            CueEditIntent.ValidateCueField(
                type = CueFieldType.TEXT,
                text = binding.tieText.text.toString(),
            )
        )
    }

    private fun updateField(event: CueEditUiEvent.UpdateField) {
        when (event.type) {
            CueFieldType.START_TIME ->
                updateFieldError(binding.tilStartTime, event.validationResult)

            CueFieldType.END_TIME -> updateFieldError(binding.tilEndTime, event.validationResult)
            CueFieldType.TEXT -> updateFieldError(binding.tilText, event.validationResult)
        }
    }

    private fun updateFieldError(inputLayout: TextInputLayout, result: ValidationResult) {
        inputLayout.isErrorEnabled = result is ValidationResult.Error
        if (result is ValidationResult.Error) {
            inputLayout.setError(result.message)
        }
        updateSaveButton()
    }

    private fun updateSaveButton() {
        binding.dialogButtons.save.isEnabled = isValidFields()
    }

    private fun isValidFields(): Boolean {
        return (binding.tilStartTime.isErrorEnabled.not() &&
                binding.tilEndTime.isErrorEnabled.not() &&
                binding.tilText.isErrorEnabled.not())
    }
}
