package com.jyodroid.kunasismoayuda

import androidx.compose.ui.window.ComposeUIViewController
import com.jyodroid.kunasismoayuda.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}