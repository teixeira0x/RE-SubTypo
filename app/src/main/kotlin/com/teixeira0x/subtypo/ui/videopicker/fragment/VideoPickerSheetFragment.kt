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

package com.teixeira0x.subtypo.ui.videopicker.fragment

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.R.attr
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.media.model.Album
import com.teixeira0x.subtypo.core.media.model.Video
import com.teixeira0x.subtypo.core.ui.base.BaseBottomSheetFragment
import com.teixeira0x.subtypo.databinding.FragmentSheetVideoPickerBinding
import com.teixeira0x.subtypo.ui.videopicker.adapter.VideoPreviewListAdapter
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerIntent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerUiEvent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerUiState
import com.teixeira0x.subtypo.ui.videopicker.util.GridSpacingItemDecoration
import com.teixeira0x.subtypo.ui.videopicker.util.videoListSortByName
import com.teixeira0x.subtypo.ui.videopicker.viewmodel.VideoPickerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class VideoPickerSheetFragment : BaseBottomSheetFragment() {

    companion object {
        @JvmStatic
        fun newSingleChoice(onChooseVideo: (Video) -> Unit): VideoPickerSheetFragment {
            return VideoPickerSheetFragment().apply { this.onChooseVideo = onChooseVideo }
        }
    }

    private val viewModel by viewModels<VideoPickerViewModel>()
    private var _binding: FragmentSheetVideoPickerBinding? = null
    private val binding: FragmentSheetVideoPickerBinding
        get() = checkNotNull(_binding) { "VideoPickerSheetFragment has been destroyed!" }

    private var onChooseVideo: ((Video) -> Unit)? = null
    private val videoPreviewListAdapter = VideoPreviewListAdapter {
        dismiss()
        onChooseVideo?.invoke(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSheetVideoPickerBinding.inflate(inflater).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        configureUI()
    }

    override fun onStart() {
        super.onStart()

        if (onChooseVideo != null) {
            viewModel.doIntent(VideoPickerIntent.Load)
        } else dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.videoPickerUiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is VideoPickerUiState.Loading -> {
                        binding.progressIndicator.isVisible = true
                    }
                    is VideoPickerUiState.Loaded -> {
                        updateChipGroupAlbums(state.albums)
                        videoPreviewListAdapter.submitList(state.videos)
                        binding.progressIndicator.isVisible = false
                    }
                    is VideoPickerUiState.Error -> {
                        binding.chipGroupAlbums.isVisible = false
                        binding.rvVideoPreviewList.isVisible = false
                        binding.progressIndicator.isVisible = false
                        binding.tvError.isVisible = true
                        binding.tvError.setText(state.message)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.customUiEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is VideoPickerUiEvent.UpdateVideoList ->
                        videoPreviewListAdapter.submitList(event.videos)
                    is VideoPickerUiEvent.ShowLoading -> {
                        binding.rvVideoPreviewList.visibility = View.INVISIBLE
                        binding.progressIndicator.isVisible = true
                    }
                    is VideoPickerUiEvent.HideLoading -> {
                        binding.rvVideoPreviewList.visibility = View.VISIBLE
                        binding.progressIndicator.isVisible = false
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateChipGroupAlbums(albums: List<Album>) {
        binding.chipGroupAlbums.removeAllViews()
        albums.forEach { album ->
            val chip =
                Chip(requireContext()).apply {
                    text = album.name
                    isClickable = true
                    isCheckable = true
                    isChecked = album.id == viewModel.currentAlbumId
                    setOnClickListener {
                        if (viewModel.currentAlbumId != album.id) {
                            viewModel.doIntent(VideoPickerIntent.LoadVideos(album.id))
                        }
                        updateSelectedChip(this)
                    }
                }
            binding.chipGroupAlbums.addView(chip)
        }
    }

    private fun updateSelectedChip(selectedChip: Chip) {
        for (i in 0 until binding.chipGroupAlbums.childCount) {
            val chip = binding.chipGroupAlbums.getChildAt(i) as? Chip
            chip?.isChecked = chip == selectedChip
        }
    }

    private fun configureUI() {
        val spanCount =
            resources.getInteger(
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    R.integer.video_picker_grid_span_count_landscape
                } else R.integer.video_picker_grid_span_count
            )
        val spacing = 10

        binding.rvVideoPreviewList.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.rvVideoPreviewList.adapter = videoPreviewListAdapter
        binding.rvVideoPreviewList.itemAnimator = null

        binding.rvVideoPreviewList.addItemDecoration(
            GridSpacingItemDecoration(spanCount, spacing, false)
        )

        TooltipCompat.setTooltipText(binding.imgClose, binding.imgClose.contentDescription)

        TooltipCompat.setTooltipText(
            binding.imgToggleSort,
            binding.imgToggleSort.contentDescription,
        )

        binding.imgClose.setOnClickListener { dismiss() }
        binding.imgToggleSort.setOnClickListener {
            videoListSortByName = !videoListSortByName
            updateImgToggleSortTint()
            viewModel.doIntent(VideoPickerIntent.Load)
        }
        updateImgToggleSortTint()
    }

    private fun updateImgToggleSortTint() {
        binding.imgToggleSort.imageTintList =
            ColorStateList.valueOf(
                MaterialColors.getColor(
                    binding.imgToggleSort,
                    if (videoListSortByName) attr.colorPrimary else attr.colorControlNormal,
                )
            )
    }
}
