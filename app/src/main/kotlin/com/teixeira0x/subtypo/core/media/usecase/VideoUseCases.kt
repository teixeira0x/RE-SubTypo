package com.teixeira0x.subtypo.core.media.usecase

import com.teixeira0x.subtypo.core.media.model.Album
import com.teixeira0x.subtypo.core.media.model.Video
import com.teixeira0x.subtypo.core.media.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(private val repository: VideoRepository) {

    operator suspend fun invoke(albumId: String?): Flow<List<Video>> {
        return repository.getVideos(albumId)
    }
}

class GetAlbumsUseCase @Inject constructor(private val repository: VideoRepository) {

    operator suspend fun invoke(): Flow<List<Album>> {
        return repository.getAlbums()
    }
}
