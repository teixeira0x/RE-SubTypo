package com.teixeira0x.subtypo.ui.project.activity

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.ui.base.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.databinding.ActivityProjectBinding
import com.teixeira0x.subtypo.ui.textlist.fragment.CueListFragment
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListIntent
import com.teixeira0x.subtypo.ui.textlist.mvi.CueListUiEvent
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerUiEvent
import com.teixeira0x.subtypo.ui.videoplayer.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectActivity : BaseEdgeToEdgeActivity() {
    private val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()
    private val cueListViewModel by viewModels<CueListViewModel>()

    private val openSubtitleFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    readSubtitle(uri)
                }

            }
        }
    private var _binding: ActivityProjectBinding? = null

    private val binding: ActivityProjectBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed!" }

    override fun bindView(): View {
        return ActivityProjectBinding.inflate(layoutInflater).also { _binding = it }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        menuInflater.inflate(R.menu.activity_project_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_subtitle -> openSubtitleFileLauncher.launch("*/*")
            R.id.menu_close -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        _binding?.apply {
            appBar.updatePadding(top = insets.top)
            toolbar.updatePaddingRelative(start = insets.left, end = insets.right)

            mainContent.updatePadding(left = insets.left)

            fragmentCueList.getFragment<CueListFragment>().onApplySystemBarInsets(insets)
        }
    }


    private fun observeViewModel() {
        videoPlayerViewModel.playerPosition.observe(this) { position ->
            cueListViewModel.updatePlayerPosition(position)
        }

        cueListViewModel.customUiEvent
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is CueListUiEvent.PlayerUpdateSubtitle ->
                        videoPlayerViewModel.setSubtitle(event.subtitle)

                    is CueListUiEvent.PlayerPause ->
                        videoPlayerViewModel.doEvent(VideoPlayerUiEvent.Pause)

                    is CueListUiEvent.PlayerSeekTo ->
                        videoPlayerViewModel.doEvent(VideoPlayerUiEvent.SeekTo(event.position))

                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)
    }

    private suspend fun readSubtitle(uri: Uri) {
        try {
            val fileName =
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        cursor.getString(nameIndex)
                    } else null
                } ?: uri.lastPathSegment
            val extension = fileName?.substringAfterLast('.', "")

            val content =
                try {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                } catch (e: Exception) {
                    null
                }

            if (
                fileName != null && extension != null && content != null && !content.isEmpty()
            ) {
                val (subtitleFormat, parseResult) =
                    try {
                        SubtitleFormat.of(".$extension", content)
                    } catch (error: Throwable) {
                        return
                    }

                cueListViewModel.doIntent(
                    CueListIntent.LoadSubtitle(
                        Subtitle(name = fileName, format = subtitleFormat, data = parseResult.data)
                    )
                )
            }
        } catch (e: Exception) {

        }
    }
}
