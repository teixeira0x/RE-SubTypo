package com.teixeira0x.subtypo.core.media.model

import android.net.Uri

data class Video(
    val id: String,
    val title: String,
    val displayName: String,
    val duration: Long = 0,
    val albumName: String,
    val size: String,
    val corrupted: Boolean = false,
    val path: String,
    val videoUri: Uri,
)
