package com.teixeira0x.subtypo.core.subtitle.exception

sealed class SubtitleException(message: String, cause: Throwable? = null) :
    Throwable(message, cause)

class UnknownFormatException(message: String, cause: Throwable? = null) :
    SubtitleException(message, cause)
