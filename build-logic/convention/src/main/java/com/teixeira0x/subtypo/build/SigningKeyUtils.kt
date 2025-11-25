package com.teixeira0x.subtypo.build

import java.util.Base64
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

private const val KEY_BASE64 = "SIGNING_KEY_BASE64"
private const val KEY_PASSWORD = "SIGNING_KEY_PASSWORD"
private const val KEY_ALIAS = "SIGNING_KEY_ALIAS"

val Project.signingKeyFile: Provider<RegularFile>
    get() = rootProject.layout.buildDirectory.file("signing-key.jks")

object SigningKeyUtils {

    fun Project.writeSigningKey() {
        val signingKey = signingKeyFile.get().asFile
        if (signingKey.exists()) {
            return
        }
        signingKey.parentFile?.mkdirs()

        getEnvOrProp(key = KEY_BASE64)?.also { bin ->
            signingKey.writeBytes(Base64.getDecoder().decode(bin))
        }
    }

    fun Project.getSigningKeyAlias(): String? {
        return getEnvOrProp(KEY_ALIAS)
    }

    fun Project.getSigningKeyPass(): String? {
        return getEnvOrProp(KEY_PASSWORD)
    }
}
