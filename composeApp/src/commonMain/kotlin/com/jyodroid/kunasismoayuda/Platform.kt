package com.jyodroid.kunasismoayuda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform