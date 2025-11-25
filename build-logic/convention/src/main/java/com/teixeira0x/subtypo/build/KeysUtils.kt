package com.teixeira0x.subtypo.build

import org.gradle.api.Project

private const val KEY_APP_API = "APP_API_KEY"

val Project.appApiKey: String
    get() = getEnvOrProp(KEY_APP_API) ?: ""

fun Project.getEnvOrProp(key: String): String? {
    var value: String? = System.getenv(key)
    if (value.isNullOrBlank()) {
        value = project.properties[key] as? String?
    }
    return value
}
