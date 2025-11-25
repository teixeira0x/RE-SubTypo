package com.teixeira0x.subtypo.core.data.di

import com.teixeira0x.subtypo.core.data.repository.*
import com.teixeira0x.subtypo.core.media.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindsVideoRepository(repository: VideoRepositoryImpl): VideoRepository

}
