package com.teixeira0x.subtypo.core.ui.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import com.teixeira0x.subtypo.core.ui.util.applySystemBarsInsets

abstract class BaseEdgeToEdgeActivity : BaseActivity() {

    protected open var edgeToEdgeConsumed: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.decorView.applySystemBarsInsets(edgeToEdgeConsumed) { insets ->
            onApplySystemBarInsets(insets)
        }
    }

    abstract fun onApplySystemBarInsets(insets: Insets)
}
