package com.teixeira0x.subtypo.core.data.resource

import android.content.Context
import com.teixeira0x.subtypo.core.resource.ResourcesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResourcesManagerImpl @Inject constructor(@ApplicationContext val context: Context) :
    ResourcesManager {

    private val res = context.resources

    override fun getString(resId: Int, vararg args: Any): String {
        return res.getString(resId, *args)
    }

    override fun getQuantityString(resId: Int, count: Int, vararg args: Any): String {
        return res.getQuantityString(resId, count, *args)
    }
}
