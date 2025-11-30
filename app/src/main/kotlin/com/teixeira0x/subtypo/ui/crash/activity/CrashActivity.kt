package com.teixeira0x.subtypo.ui.crash.activity

import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.DeviceUtils
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.core.ui.base.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.databinding.ActivityCrashBinding
import java.util.Date

class CrashActivity : BaseEdgeToEdgeActivity() {

    companion object {
        const val KEY_EXTRA_CRASH_ERROR = "key_extra_error"
    }

    private var _binding: ActivityCrashBinding? = null
    private val binding: ActivityCrashBinding
        get() = _binding!!

    private val softwareInfo: String
        get() =
            StringBuilder("Manufacturer: ")
                .append(DeviceUtils.getManufacturer())
                .append("\n")
                .append("Device: ")
                .append(DeviceUtils.getModel())
                .append("\n")
                .append("SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append("\n")
                .append("Android: ")
                .append(Build.VERSION.RELEASE)
                .append("\n")
                .append("Model: ")
                .append(Build.VERSION.INCREMENTAL)
                .append("\n")
                .toString()

    private val appInfo: String
        get() =
            StringBuilder("Version: ")
                .append(BuildConfig.VERSION_NAME)
                .append(". ")
                .append("Build type: ")
                .append(BuildConfig.BUILD_TYPE)
                .toString()

    private val date: Date
        get() = Calendar.getInstance().time

    override fun bindView(): View {
        return ActivityCrashBinding.inflate(layoutInflater).also { _binding = it }.root
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
        setSupportActionBar(binding.toolbar)

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity()
                }
            }
        )

        binding.apply {
            tvError.text = StringBuilder()
                .append("$appInfo\n")
                .append("$softwareInfo\n")
                .append("$date\n\n")
                .append(intent.getStringExtra(KEY_EXTRA_CRASH_ERROR))
                .toString()

            btnCopyAndReport.setOnClickListener {
                ClipboardUtils.copyText(tvError.text.toString())
                // openUrl(App.APP_REPO_OPEN_ISSUE_URL)
            }
            btnCopy.setOnClickListener { ClipboardUtils.copyText(tvError.text.toString()) }
            btnCloseApp.setOnClickListener { finishAffinity() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
