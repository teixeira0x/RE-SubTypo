package com.teixeira0x.subtypo.core.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.teixeira0x.subtypo.core.ui.fragment.ProgressDialogFragment
import com.teixeira0x.subtypo.core.ui.util.updateTheme

/**
 * Base activity for all application activities.
 *
 * @author Felipe Teixeira
 */
abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialogFragment? = null

    protected abstract fun bindView(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)
        setContentView(bindView())
    }

    protected fun showProgressDialog(
        orientation: Int = ProgressDialogFragment.ORIENTATION_VERTICAL,
        style: Int = ProgressDialogFragment.STYLE_NO_BACKGROUND,
        cancelable: Boolean = false,
        message: String? = null,
    ) {
        ProgressDialogFragment.newInstance(
                orientation = orientation,
                style = style,
                cancelable = cancelable,
                message = message,
            )
            .also {
                progressDialog?.dismiss() // Dismiss previous dialog
                progressDialog = it
            }
            .show(supportFragmentManager, null)
    }

    protected fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}
