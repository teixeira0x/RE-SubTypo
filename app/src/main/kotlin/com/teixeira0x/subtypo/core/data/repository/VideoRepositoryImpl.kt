package com.teixeira0x.subtypo.core.data.repository

import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import com.teixeira0x.subtypo.core.media.model.Album
import com.teixeira0x.subtypo.core.media.model.Video
import com.teixeira0x.subtypo.core.media.repository.VideoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.slf4j.LoggerFactory

class VideoRepositoryImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    VideoRepository {

    companion object {
        private val log = LoggerFactory.getLogger(VideoRepositoryImpl::class.java)

        private val VIDEO_PROJECTION =
            arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_ID,
            )

        private val ALBUM_PROJECTION =
            arrayOf(MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
    }

    override fun getVideos(albumId: String?) =
        flow<List<Video>> {
                val videoList = mutableListOf<Video>()

                val selection =
                    when {
                        !albumId.isNullOrEmpty() -> "${MediaStore.Video.Media.BUCKET_ID} = ?"
                        else -> null
                    }

                val selectionArgs =
                    when {
                        !albumId.isNullOrEmpty() -> arrayOf(albumId)
                        else -> null
                    }

                appContext.contentResolver
                    .query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        VIDEO_PROJECTION,
                        selection,
                        selectionArgs,
                        "${MediaStore.Video.Media.DATE_ADDED} DESC",
                    )
                    ?.use { cursor ->
                        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                        val displayNameIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                        val albumIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        val durationIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                        while (cursor.moveToNext()) {
                            try {
                                val path = cursor.getString(pathIndex)
                                val file = File(path)
                                if (file.exists()) {
                                    videoList.add(
                                        Video(
                                            title = cursor.getString(titleIndex),
                                            displayName = cursor.getString(displayNameIndex),
                                            id = cursor.getString(idIndex),
                                            duration = cursor.getLong(durationIndex),
                                            albumName = cursor.getString(albumIndex),
                                            size = cursor.getString(sizeIndex),
                                            path = path,
                                            videoUri = file.toUri(),
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                log.error("Error processing video", e)
                            }
                        }
                    } ?: log.warn("Cursor is null")

                emit(videoList)
            }
            .flowOn(Dispatchers.IO)

    override fun getAlbums() =
        flow<List<Album>> {
                val albumList = mutableListOf<Album>()
                val albumSet = mutableSetOf<String>()

                appContext.contentResolver
                    .query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        ALBUM_PROJECTION,
                        null,
                        null,
                        null,
                    )
                    ?.use { cursor ->
                        val albumIdIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                        val albumNameIndex =
                            cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                        if (albumIdIndex == -1 || albumNameIndex == -1) {
                            log.warn("Required columns not found in MediaStore")
                            return@use
                        }

                        while (cursor.moveToNext()) {
                            val albumId = cursor.getString(albumIdIndex) ?: continue
                            val albumName = cursor.getString(albumNameIndex) ?: "Unknown"

                            if (albumSet.add(albumId)) {
                                albumList.add(Album(id = albumId, name = albumName))
                            }
                        }
                    } ?: log.warn("Cursor is null")

                emit(albumList)
            }
            .flowOn(Dispatchers.IO)
}
