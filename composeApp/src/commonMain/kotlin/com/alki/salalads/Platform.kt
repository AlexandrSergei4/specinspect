package com.alki.salalads

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform