package com.teixeira0x.subtypo.ui.about.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import com.teixeira0x.subtypo.R
import com.teixeira0x.subtypo.core.ui.base.BaseBottomSheetFragment
import com.teixeira0x.subtypo.databinding.FragmentSheetAboutLibrariesBinding

class AboutLibrariesSheetFragment : BaseBottomSheetFragment() {

    private var fragment: WithoutInsetsLibsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return FragmentSheetAboutLibrariesBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, WithoutInsetsLibsFragment().also { fragment = it })
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragment = null
    }

    class WithoutInsetsLibsFragment : LibsSupportFragment() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                view.setPadding(0, 0, 0, 0)
                insets
            }
        }
    }
}
