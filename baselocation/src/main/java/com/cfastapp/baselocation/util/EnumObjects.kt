package com.cfastapp.baselocation.util

enum class ResponseEnabledGPS(val code: Int) {
    DISABLED(0), ENABLED(-1);

    companion object {
        fun digitCode(findValue: Int): ResponseEnabledGPS = values().first { it.code == findValue }
    }
}