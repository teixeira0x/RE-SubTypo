package com.teixeira0x.subtypo.core.media.repository

import com.teixeira0x.subtypo.core.media.model.Album
import com.teixeira0x.subtypo.core.media.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {

    fun getVideos(albumId: String?): Flow<List<Video>>

    fun getAlbums(): Flow<List<Album>>
}
