package com.teixeira0x.subtypo.ui.sourceview.mvp

import com.teixeira0x.subtypo.core.subtitle.model.Subtitle

sealed class SourceViewIntent {
    data class LoadSubtitle(val subtitle: Subtitle) : SourceViewIntent()
}