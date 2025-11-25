package com.teixeira0x.subtypo.core.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import java.io.File

fun Context.openUrl(url: String) {
    startActivity(
        Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        }
    )
}

fun Context.shareFile(
    file: File,
    mimeType: String = "*/*",
    intentAction: String = Intent.ACTION_SEND,
) {
    val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
    val intent =
        ShareCompat.IntentBuilder(this)
            .setType(mimeType)
            .setStream(uri)
            .intent
            .setAction(intentAction)
            .setDataAndType(uri, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    startActivity(Intent.createChooser(intent, null))
}
