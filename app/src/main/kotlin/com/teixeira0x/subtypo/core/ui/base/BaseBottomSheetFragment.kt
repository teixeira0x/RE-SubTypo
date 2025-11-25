package com.teixeira0x.subtypo.core.ui.base

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teixeira0x.subtypo.R

/**
 * Base class for BottomSheet fragments.
 *
 * @author Felipe Teixeira
 */
abstract class BaseBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.ThemeOverlay_SubTypo_BottomSheetDialog)
            .apply {
                behavior.apply {
                    peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
    }
}
