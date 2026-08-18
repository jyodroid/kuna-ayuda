package com.jyodroid.kunasismoayuda

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jyodroid.kunasismoayuda.core.beacon.AndroidBeaconContext
import com.jyodroid.kunasismoayuda.core.data.offline.AndroidOutboxContext
import com.jyodroid.kunasismoayuda.core.location.AndroidLocationContext
import com.jyodroid.kunasismoayuda.di.initKoin

class MainActivity : ComponentActivity() {

    private val requestLocation = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* result handled lazily when the user triggers SOS */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set platform contexts before Koin starts so the outbox reads its persisted file on launch.
        AndroidLocationContext.appContext = applicationContext
        AndroidOutboxContext.appContext = applicationContext
        AndroidBeaconContext.appContext = applicationContext
        initKoin()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Ask for location up front so an SOS can attach precise coordinates.
        requestLocation.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}