package com.teixeira0x.subtypo.ui.editor.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.preference.PreferencesManager
import com.teixeira0x.subtypo.core.preference.PreferencesManager.KEY_APPEARANCE_AMOLED
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.core.subtitle.model.SubtitleData
import com.teixeira0x.subtypo.core.ui.base.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.core.ui.util.getFileName
import com.teixeira0x.subtypo.core.ui.util.readFile
import com.teixeira0x.subtypo.core.ui.util.showToastLong
import com.teixeira0x.subtypo.core.ui.util.writeFile
import com.teixeira0x.subtypo.databinding.ActivityEditorBinding
import com.teixeira0x.subtypo.ui.optionlist.dialog.showOptionListDialog
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem
import com.teixeira0x.subtypo.ui.preference.SettingsActivity
import com.teixeira0x.subtypo.ui.sourceview.fragment.SourceViewFragment
import com.teixeira0x.subtypo.ui.sourceview.viewmodel.SourceViewModel
import com.teixeira0x.subtypo.ui.textlist.fragment.CueListFragment
import com.teixeira0x.subtypo.ui.textlist.viewmodel.CueListViewModel
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerIntent
import com.teixeira0x.subtypo.ui.videoplayer.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class EditorActivity : BaseEdgeToEdgeActivity(), OnSharedPreferenceChangeListener {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()
    private val cueListViewModel by viewModels<CueListViewModel>()
    private val sourceViewModel by viewModels<SourceViewModel>()

    private val openSubtitleFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    readSubtitle(uri)
                }
            }
        }

    private val saveSubtitleLauncher = registerForActivityResult(CreateDocument("*/*")) { uri ->
        uri?.let { saveSubtitleFile(it) }
    }

    private var _binding: ActivityEditorBinding? = null

    private val binding: ActivityEditorBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed!" }

    override fun bindView(): View {
        return ActivityEditorBinding.inflate(layoutInflater).also { _binding = it }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        PreferencesManager.registerOnSharedPreferenceChangeListener(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

//        scope.launch(Dispatchers.IO) {
//            loadBackup()
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
        PreferencesManager.registerOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        menuInflater.inflate(R.menu.activity_project_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_subtitle -> openSubtitleFileLauncher.launch("*/*")
            R.id.menu_save_subtitle, R.id.menu_save_as_subtitle -> saveSubtitleLauncher.launch(
                cueListViewModel.subtitleName + cueListViewModel.subtitleFormat.extension
            )

            R.id.menu_video_select_video -> videoPlayerViewModel.doEvent(VideoPlayerIntent.SelectVideo)
            R.id.menu_video_remove_video -> videoPlayerViewModel.loadVideo("")

            R.id.menu_subtitle_format -> {
                val allFormatsOptions = SubtitleFormat.allSubtitleFormats.map {
                    OptionItem(
                        R.drawable.ic_subtitle, "${it.name} (${it.extension})"
                    )
                }

                showOptionListDialog(
                    this, getString(R.string.subtitle_select_format), allFormatsOptions
                ) { pos, _ ->
                    cueListViewModel.setSubtitleFormat(SubtitleFormat.of(pos))
                    sourceViewModel.setSubtitleFormat(SubtitleFormat.of(pos))
                }
            }

            R.id.menu_subtitle_go_to_source_view -> {
                val isSourceVisible = binding.fragmentSourceView.isVisible
                binding.fragmentSourceView.isVisible = !isSourceVisible
                binding.fragmentSourceView.getFragment<SourceViewFragment>()
                    .onVisibilityToggle(!isSourceVisible)
                if (!isSourceVisible) {
                    sourceViewModel.updateSubtitle(
                        Subtitle(
                            name = cueListViewModel.subtitleName,
                            format = cueListViewModel.subtitleFormat,
                            data = SubtitleData(
                                cues = cueListViewModel.cues.value!!,
                                extras = cueListViewModel.subtitleExtras
                            )
                        )
                    )

                }

            }

            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        _binding?.apply {
            appBar.updatePadding(top = insets.top)
            toolbar.updatePaddingRelative(start = insets.left, end = insets.right)

            mainContent.updatePadding(left = insets.left)

            fragmentCueList.getFragment<CueListFragment>().onApplySystemBarInsets(insets)
            fragmentSourceView.getFragment<SourceViewFragment>().onApplySystemBarInsets(insets)
        }
    }

    private suspend fun readSubtitle(uri: Uri) {
        try {
            val fileName = getFileName(uri)
            val extension = fileName?.substringAfterLast('.', "")
            val content = readFile(uri)

            if (fileName != null && extension != null && content != null && !content.isEmpty()) {
                val (subtitleFormat, parseResult) = try {
                    SubtitleFormat.of(".$extension", content)
                } catch (error: Throwable) {
                    return
                }



                cueListViewModel.loadSubtitle(
                    name = fileName.substringBeforeLast("."),
                    format = subtitleFormat,
                    cues = parseResult.data.cues,
                    extras = parseResult.data.extras
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun saveSubtitleFile(uri: Uri) {
        scope.launch(Dispatchers.IO) {
            val subtitle = Subtitle(
                name = cueListViewModel.subtitleName,
                format = cueListViewModel.subtitleFormat,
                data = SubtitleData(
                    cues = cueListViewModel.cues.value!!,
                    extras = cueListViewModel.subtitleExtras
                )
            )
            val content = subtitle.toText()
            val success = writeFile(uri, content)

            if (success) {
                val fileName = getFileName(uri)
                if (fileName != null) {
                    cueListViewModel.setSubtitleName(fileName.substringBeforeLast("."))
                }
            }

            withContext(Dispatchers.Main) {
                showToastLong(
                    if (success) {
                        R.string.subtitle_share_save_file_success
                    } else {
                        R.string.subtitle_share_save_file_failed
                    }
                )
            }
        }
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?, key: String?
    ) {
        if (key == KEY_APPEARANCE_AMOLED) recreate()
    }
}
