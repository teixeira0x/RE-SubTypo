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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat
import com.teixeira0x.subtypo.core.subtitle.util.containsErrors
import com.teixeira0x.subtypo.databinding.FragmentSourceTextBinding
import com.teixeira0x.subtypo.ui.sourceview.viewmodel.SourceViewViewModel
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListIntent
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SourceViewFragment : Fragment() {
    private val viewModel by activityViewModels<SourceViewViewModel>()
    private val cueLIstViewModel by activityViewModels<CueListViewModel>()
    private var _binding: FragmentSourceTextBinding? = null

    private val binding: FragmentSourceTextBinding
        get() = _binding!!

    private val mainHandler = Handler(Looper.getMainLooper())
    private var textChangeAction: Runnable? = null
    private var insets: Insets? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentSourceTextBinding.inflate(inflater).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textChangeAction = Runnable { onTextChangeAction() }

        viewModel.sourceTextUiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                if (event.isFromUi) return@onEach

                binding.editor.setText(event.text)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.editor.addTextChangedListener { editable ->
            textChangeAction?.let { mainHandler.removeCallbacks(it) }
            textChangeAction?.let { mainHandler.postDelayed(it, 500) }
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
            val subtitle = cueLIstViewModel.subtitle

            val (subtitleFormat, parseResult) = SubtitleFormat.of(
                subtitle.format.extension, binding.editor.text.toString()
            )

            if (parseResult.diagnostics.containsErrors()) {
                return@launch
            }

            cueLIstViewModel.doIntent(
                CueListIntent.LoadSubtitle(
                    subtitle.copy(
                        format = subtitleFormat, data = parseResult.data
                    ), false
                )
            )
        }
    }
}