package com.teixeira0x.subtypo.core.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.nio.charset.StandardCharsets


fun Context.writeTempFile(name: String, content: String): File {
    val file = File(this.filesDir, name)
    if (file.exists()) {
        file.delete()
    }

    file.writeText(content, StandardCharsets.UTF_8)
    return file
}

fun Context.writeFile(fileUri: Uri, content: String): Boolean {
    return contentResolver.openOutputStream(fileUri, "w")?.bufferedWriter()?.use { writer ->
        try {
            writer.write(content)
            true
        } catch (e: Exception) {
            false
        }
    } ?: false
}

fun Context.getFileName(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            cursor.getString(nameIndex)
        } else null
    } ?: uri.lastPathSegment
}

fun Context.readFile(uri: Uri): String? {
    return try {
        contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
    } catch (e: Exception) {
        null
    }
}
