package com.jyodroid.kunasismoayuda.media

import androidx.compose.runtime.Composable

/** An image chosen by the user, already downscaled/compressed, ready to upload. */
data class PickedImage(val bytes: ByteArray, val mime: String)

/** Launches the platform gallery / camera. Obtain via [rememberImagePicker]. */
interface ImagePicker {
    /** False where there's no camera (e.g. Desktop) — hide the "take photo" button. */
    val cameraAvailable: Boolean
    fun pickFromGallery()
    fun captureFromCamera()
}

/**
 * Remembers a platform image picker. [onResult] receives the picked image (downscaled to ~1280 px,
 * JPEG) or null if the user cancelled. Android uses the system photo picker + camera-preview intent;
 * iOS uses `UIImagePickerController`; Desktop offers a file chooser (no camera).
 */
@Composable
expect fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker

/** Best-effort image mime from magic bytes; defaults to image/jpeg. */
fun sniffMime(bytes: ByteArray): String = when {
    bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() ->
        "image/jpeg"
    bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
        bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() -> "image/png"
    bytes.size >= 12 && bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() &&
        bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() && bytes[8] == 0x57.toByte() &&
        bytes[9] == 0x45.toByte() && bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte() ->
        "image/webp"
    else -> "image/jpeg"
}
