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

package com.teixeira0x.subtypo.ui.sourceview.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.doOnLayout
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.teixeira0x.subtypo.core.subtitle.util.containsErrors
import com.teixeira0x.subtypo.databinding.FragmentSourceTextBinding
import com.teixeira0x.subtypo.ui.sourceview.viewmodel.SourceViewModel
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import kotlinx.coroutines.launch

class SourceViewFragment : Fragment() {
    private val cueListViewModel by activityViewModels<CueListViewModel>()
    private val sourceViewModel by activityViewModels<SourceViewModel>()
    private var _binding: FragmentSourceTextBinding? = null

    private val binding: FragmentSourceTextBinding
        get() = _binding!!

    private val mainHandler = Handler(Looper.getMainLooper())
    private var textChangeAction: Runnable? = null
    private var insets: Insets? = null

    private var isVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentSourceTextBinding.inflate(inflater).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textChangeAction = Runnable { onTextChangeAction() }

        sourceViewModel.subtitle.observe(this) {
            binding.editor.setText(it.toText())
        }

        binding.editor.addTextChangedListener { editable ->
            if (isVisible) {
                textChangeAction?.let { mainHandler.removeCallbacks(it) }
                textChangeAction?.let { mainHandler.postDelayed(it, 500) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textChangeAction?.let { mainHandler.removeCallbacks(it) }
        textChangeAction = null
        _binding = null
        insets = null
    }

    fun onApplySystemBarInsets(insets: Insets) {
        if (_binding == null) {
            this.insets = insets
            return
        }

        binding.apply {
            bottomAppBar.updatePaddingRelative(end = insets.right, bottom = insets.bottom)
            bottomAppBar.doOnLayout { view ->
                editor.updatePaddingRelative(end = insets.right, bottom = view.height + 20)
            }
        }
    }

    private fun onTextChangeAction() {
        lifecycleScope.launch {
            val subtitle = sourceViewModel.subtitle.value!!

            val result = subtitle.format.parseText(binding.editor.text.toString())

            if (result.diagnostics.containsErrors()) {
                return@launch
            }

            cueListViewModel.setCues(result.data.cues)
        }
    }

    fun onVisibilityToggle(isVisible: Boolean) {
        this.isVisible = isVisible
    }
}