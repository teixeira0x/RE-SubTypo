package com.teixeira0x.subtypo.core.subtitle.util

import com.teixeira0x.subtypo.core.subtitle.model.Diagnostic

/**
 * Returns true if the list of diagnostics contains diagnostics with the kind `ERROR` or `OTHER`.
 */
fun List<Diagnostic>.containsErrors(): Boolean {
    return any { it.kind == Diagnostic.Kind.ERROR || it.kind == Diagnostic.Kind.OTHER }
}
