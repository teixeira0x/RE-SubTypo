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

package com.teixeira0x.subtypo.ui.textlist.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.databinding.FragmentCueListBinding
import com.teixeira0x.subtypo.ui.textedit.fragment.CueEditSheetFragment
import com.teixeira0x.subtypo.ui.textlist.adapter.CueClickListener
import com.teixeira0x.subtypo.ui.textlist.adapter.CueListAdapter
import com.teixeira0x.subtypo.ui.textlist.adapter.CueTimeClickListener
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListUiEvent
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import com.teixeira0x.subtypo.ui.videoplayer.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CueListFragment : Fragment(), CueClickListener, CueTimeClickListener {
    private val cueListViewModel by activityViewModels<CueListViewModel>()
    private val playerViewModel by activityViewModels<VideoPlayerViewModel>()

    private var _binding: FragmentCueListBinding? = null
    private val binding: FragmentCueListBinding
        get() = checkNotNull(_binding) { "CueListFragment has been destroyed!" }

    private val handler = Handler(Looper.getMainLooper())
    private val cuesAdapter = CueListAdapter(this, this)

    private var insets: Insets? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentCueListBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        configureUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        insets = null
    }

    private fun observeViewModel() {
        cueListViewModel.customUiEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is CueListUiEvent.ScrollTo -> binding.rvCues.scrollToPosition(event.index)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        playerViewModel.playerPosition.observe(this) { position ->
            cuesAdapter.updateVisibleCues(position)
        }

        cueListViewModel.cues.observe(this) { cues ->
            binding.tvCuesEmpty.isVisible = cues.isEmpty()
            binding.rvCues.isVisible = cues.isNotEmpty()
            cuesAdapter.submitList(cues)
        }
    }

    private fun showCueEditSheet(cueIndex: Int = -1) {
        playerViewModel.pause()
        // Create the fragment with a delay to wait for the video to pause.
        handler.postDelayed(
            Runnable {
                CueEditSheetFragment.newInstance(
                    playerPosition = playerViewModel.playerPosition.value ?: 0L,
                    cueIndex = cueIndex,
                )
                    .show(childFragmentManager, "CueEditSheetFragment")
            },
            50L,
        )
    }

    private fun configureUI() {
        binding.fabAddCue.setOnClickListener { showCueEditSheet() }
        binding.bottomAppBar.apply {
            setNavigationOnClickListener { cueListViewModel.sortCueListByTime() }
        }

        binding.rvCues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCues.adapter = cuesAdapter

        // Sometimes the activity applies the insets before creating the fragment,
        // so to ensure that the insets are applied to that fragment, we call it
        // again when creating it and setting up the views.
        insets?.let { onApplySystemBarInsets(it) }
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
                rvCues.updatePaddingRelative(end = insets.right, bottom = view.height)
            }
        }
    }

    override fun onCueClick(index: Int, cue: Cue) {
        showCueEditSheet(index)
    }

    override fun onCueStartTimeClick(startTime: Long) {
        playerViewModel.seekTo(startTime)
    }

    override fun onCueEndTimeClick(endTime: Long) {
        playerViewModel.seekTo(endTime)
    }
}
