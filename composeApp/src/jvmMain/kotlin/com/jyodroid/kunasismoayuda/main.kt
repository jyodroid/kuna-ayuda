package com.jyodroid.kunasismoayuda

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jyodroid.kunasismoayuda.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Kuna Ayuda",
        ) {
            App()
        }
    }
}