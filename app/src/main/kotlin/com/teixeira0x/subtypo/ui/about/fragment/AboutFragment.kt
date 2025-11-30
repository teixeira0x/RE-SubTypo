package com.teixeira0x.subtypo.ui.about.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.Urls
import com.teixeira0x.subtypo.core.ui.util.openUrl
import com.teixeira0x.subtypo.databinding.FragmentAboutBinding
import com.teixeira0x.subtypo.ui.about.adapter.CardItemListAdapter
import com.teixeira0x.subtypo.ui.about.model.CardItem

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding: FragmentAboutBinding
        get() = checkNotNull(_binding) { "AboutFragment has been destroyed" }

    private val appVersion = "v${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentAboutBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAppVersion.text = appVersion

        binding.rvContribution.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContribution.adapter = CardItemListAdapter(getContributionCardItemList())

        binding.rvMore.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMore.adapter = CardItemListAdapter(getMoreCardItemList())
    }

    private fun getContributionCardItemList(): List<CardItem> {
        return listOf(
            CardItem(
                icon = R.drawable.ic_translate,
                title = getString(R.string.about_title_translate),
                subtitle = getString(R.string.about_subtitle_translate),
                action = { openUrl(Urls.APP_CROWDIN_URL) },
            )
        )
    }

    private fun getMoreCardItemList(): List<CardItem> {
        return listOf(
            CardItem(
                icon = R.drawable.ic_license,
                title = getString(R.string.about_title_libraries),
                subtitle = getString(R.string.about_subtitle_libraries),
                action = {
                    AboutLibrariesSheetFragment()
                        .show(childFragmentManager, "AboutLibrariesSheetFragment")
                },
            ),
        )
    }

    private fun openUrl(url: String) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(getString(R.string.open_url_info_msg, url))
            setNegativeButton(R.string.no, null)
            setPositiveButton(R.string.yes) { _, _ -> context?.openUrl(url) }
            show()
        }
    }
}
