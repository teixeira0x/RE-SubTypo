package com.teixeira0x.subtypo.core.data.di

import com.teixeira0x.subtypo.core.data.resource.ResourcesManagerImpl
import com.teixeira0x.subtypo.core.resource.ResourcesManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourcesModule {

    @Binds
    @Singleton
    abstract fun bindResourcesManager(resourcesManagerImpl: ResourcesManagerImpl): ResourcesManager
}
