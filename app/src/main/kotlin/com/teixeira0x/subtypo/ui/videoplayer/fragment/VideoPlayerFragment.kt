package com.teixeira0x.subtypo.ui.videoplayer.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.CaptioningManager
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED
import androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY
import androidx.media3.common.Player.EVENT_TIMELINE_CHANGED
import androidx.media3.common.Player.Events
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import com.blankj.utilcode.util.ClipboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.core.ui.permission.StoragePermissions
import com.teixeira0x.subtypo.databinding.FragmentPlayerBinding
import com.teixeira0x.subtypo.ui.videopicker.fragment.VideoPickerSheetFragment
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerIntent
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerUiEvent
import com.teixeira0x.subtypo.ui.videoplayer.util.PlayerErrorMessageProvider
import com.teixeira0x.subtypo.ui.videoplayer.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.media3.common.text.Cue as ExoCue

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {

    companion object {
        private const val DEFAULT_SEEK_BACK_MS = 5_000L
        private const val DEFAULT_SEEK_FORWARD_MS = 5_000L

        private val PLAYBACK_SPEEDS = arrayOf<Float>(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val viewModel by activityViewModels<VideoPlayerViewModel>()

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding
        get() = checkNotNull(_binding) { "VideoPlayerFragment has been destroyed" }

    private var storagePermReq: StoragePermissions? = null
    private var updateProgressAction: Runnable? = null
    private var componentListener: ComponentListener? = null
    private var player: ExoPlayer? = null

    private val showingExoCues = mutableListOf<ExoCue>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        componentListener = ComponentListener()
        return FragmentPlayerBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
        componentListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        storagePermReq = StoragePermissions(this)

        binding.tvCurrentPosition.setOnClickListener(componentListener)
        binding.tvDuration.setOnClickListener(componentListener)
        binding.imgSkipBackward.setOnClickListener(componentListener)
        binding.imgPlay.setOnClickListener(componentListener)
        binding.imgSkipForward.setOnClickListener(componentListener)
        binding.imgPlayerVisibility.setOnClickListener(componentListener)
        binding.imgPlaybackSpeed.setOnClickListener(componentListener)

        binding.playerView.setErrorMessageProvider(PlayerErrorMessageProvider(requireContext()))
        binding.playerView.useController = false
        binding.playerView.setOnClickListener {
            // Choose video
            storagePermReq?.requestPermissions {
                VideoPickerSheetFragment.newSingleChoice { video ->
                    if (video.corrupted) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(R.string.video_player_corrupted_file)
                            .setPositiveButton(R.string.ok) { _, _ -> }
                            .show()
                        return@newSingleChoice
                    }
                    viewModel.doEvent(VideoPlayerIntent.LoadVideoUri(video.path))
                }.show(childFragmentManager, "VideoPickerSheetFragment")
            }
        }


    }

    override fun onResume() {
        super.onResume()
        configureSubtitleView()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
        _binding?.playerView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        _binding?.playerView?.onPause()
        releasePlayer()
    }

    private fun observeViewModel() {
        viewModel.customUiEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is VideoPlayerUiEvent.LoadUri -> prepareMedia(event.videoUri)

                    is VideoPlayerUiEvent.LoadSubtitle -> updateProgress()
                    is VideoPlayerUiEvent.Visibility -> updatePlayerVisibility(event.visible)
                    is VideoPlayerUiEvent.SeekTo -> player?.seekTo(event.position)
                    is VideoPlayerUiEvent.Pause -> pausePlayer()
                    is VideoPlayerUiEvent.Play -> playPlayer()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun configureSubtitleView() {
        val captioningManager =
            requireContext().getSystemService(Context.CAPTIONING_SERVICE) as CaptioningManager
        val systemCaptionStyle = captioningManager.userStyle

        val captionStyleCompat = CaptionStyleCompat.createFromCaptionStyle(systemCaptionStyle);
        binding.playerView.subtitleView?.setStyle(captionStyleCompat);
    }

    private fun updatePlayerVisibility(visible: Boolean) {
        binding.playerContainer.isVisible = visible
        binding.imgPlayerVisibility.setImageResource(
            if (visible) {
                R.drawable.ic_video_off
            } else {
                R.drawable.ic_video
            }
        )
    }

    private fun initializePlayer() {
        if (player == null) {
            updateProgressAction = Runnable { updateProgress() }
            _binding?.playerView?.player =
                ExoPlayer.Builder(requireContext())
                    .setSeekBackIncrementMs(DEFAULT_SEEK_BACK_MS)
                    .setSeekForwardIncrementMs(DEFAULT_SEEK_FORWARD_MS)
                    .build()
                    .also { player = it }

            player?.addListener(componentListener!!)
        }

        prepareMedia(viewModel.videoPath.value!!)
    }

    private fun releasePlayer() {
        binding.imgPlay.setImageResource(R.drawable.ic_play)
        updateProgressAction?.let { mainHandler.removeCallbacks(it) }
        updateProgressAction = null
        player?.release()
        player = null
    }

    private fun prepareMedia(videoUri: String) {
        player?.apply {
            clearMediaItems()
            if (videoUri.isNotEmpty()) {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()

                seekTo(viewModel.playerPosition.value!!)
            }
        }
    }

    private fun playPlayer() {
        binding.imgPlay.setImageResource(R.drawable.ic_pause)
        player?.play()
    }

    private fun pausePlayer() {
        binding.imgPlay.setImageResource(R.drawable.ic_play)
        player?.pause()
    }

    private fun updateProgress() {
        val player = player ?: return

        val currentPosition = player.currentPosition

        showingExoCues.clear()
        viewModel.currentExoCuesTimed?.forEach { exoCuesTimed ->
            if (currentPosition in exoCuesTimed.startTime..exoCuesTimed.endTime) {
                showingExoCues.addAll(exoCuesTimed.exoCues)
            }
        }

        binding.playerView.subtitleView?.setCues(showingExoCues)
        updateTimeline()

        if (player.isPlaying) {
            updateProgressAction?.let { mainHandler.post(it) }
        }
    }

    private fun updateTimeline() {
        val player = player ?: return

        val duration = player.duration
        val currentPosition = player.currentPosition

        viewModel.updatePlayerPosition(currentPosition)

        binding.seekBar.setMax(duration.toInt())
        binding.seekBar.setProgress(currentPosition.toInt())
        binding.seekBar.setOnSeekBarChangeListener(componentListener)
        binding.tvDuration.text = duration.getFormattedTime()
    }

    private fun showPlaybackSpeedPopup() {
        PopupMenu(requireContext(), binding.imgPlaybackSpeed).apply {
            if (menu is MenuBuilder) {
                (menu as MenuBuilder).setOptionalIconsVisible(true)
            }

            val playbackSpeedTexts =
                resources.getStringArray(R.array.video_player_controls_playback_speeds)
            playbackSpeedTexts.forEachIndexed { index, text ->
                val speed = PLAYBACK_SPEEDS[index]
                val newItem = menu.add(0, index, 0, text)

                if (player?.playbackParameters?.speed == speed) {
                    newItem.setIcon(R.drawable.ic_check)
                }
            }

            setOnMenuItemClickListener { item ->
                player?.setPlaybackSpeed(PLAYBACK_SPEEDS[item.itemId])
                true
            }
            show()
        }
    }

    inner class ComponentListener :
        Player.Listener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        override fun onEvents(player: Player, events: Events) {
            if (
                events.containsAny(
                    EVENT_PLAYBACK_STATE_CHANGED,
                    EVENT_PLAY_WHEN_READY_CHANGED,
                    EVENT_IS_PLAYING_CHANGED,
                    EVENT_AVAILABLE_COMMANDS_CHANGED,
                )
            ) {
                updateProgress()
            }

            if (
                events.containsAny(
                    EVENT_POSITION_DISCONTINUITY,
                    EVENT_TIMELINE_CHANGED,
                    EVENT_AVAILABLE_COMMANDS_CHANGED,
                )
            ) {
                updateTimeline()
            }
        }

        private var isPlaying = false
        private var seekProgress = 0

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            binding.tvCurrentPosition.text = progress.toLong().getFormattedTime()
            seekProgress = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            isPlaying = player?.isPlaying ?: false
            if (isPlaying) pausePlayer()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            player?.seekTo(seekProgress.toLong())
            if (isPlaying) playPlayer()
        }

        override fun onClick(view: View) {
            when (view.id) {
                binding.tvCurrentPosition.id ->
                    ClipboardUtils.copyText(binding.tvCurrentPosition.text.toString())

                binding.tvDuration.id -> ClipboardUtils.copyText(binding.tvDuration.text.toString())

                binding.imgSkipBackward.id -> player?.seekBack()
                binding.imgPlay.id -> {
                    if (player?.isPlaying == true) {
                        pausePlayer()
                    } else playPlayer()
                }

                binding.imgSkipForward.id -> player?.seekForward()

                binding.imgPlayerVisibility.id ->
                    viewModel.setPlayerVisibility(!viewModel.isPlayerVisible)

                binding.imgPlaybackSpeed.id -> showPlaybackSpeedPopup()
            }
        }
    }
}