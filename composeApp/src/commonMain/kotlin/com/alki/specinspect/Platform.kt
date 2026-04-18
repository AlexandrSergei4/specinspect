package com.alki.specinspect

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform