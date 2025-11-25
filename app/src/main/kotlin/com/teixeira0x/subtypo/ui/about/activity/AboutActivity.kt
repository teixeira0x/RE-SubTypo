package com.teixeira0x.subtypo.ui.about.activity

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.teixeira0x.subtypo.core.ui.base.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.databinding.ActivityAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutActivity : BaseEdgeToEdgeActivity() {

    private var _binding: ActivityAboutBinding? = null

    override fun bindView(): View {
        return ActivityAboutBinding.inflate(layoutInflater).also { _binding = it }.root
    }

    override fun onApplySystemBarInsets(insets: Insets) {
        _binding?.apply {
            appBar.updatePadding(top = insets.top)
            toolbar.updatePaddingRelative(start = insets.left, end = insets.right)

            mainContent.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(_binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        _binding?.toolbar?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
