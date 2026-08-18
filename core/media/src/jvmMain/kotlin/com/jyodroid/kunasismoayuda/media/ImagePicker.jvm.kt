package com.jyodroid.kunasismoayuda.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker = remember {
    object : ImagePicker {
        override val cameraAvailable = false // no camera on desktop

        override fun pickFromGallery() {
            SwingUtilities.invokeLater {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Choose an image"
                    fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "webp")
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    val img = runCatching { ImageIO.read(file) }.getOrNull()
                    onResult(
                        if (img != null) toJpeg(img) else {
                            val raw = file.readBytes()
                            PickedImage(raw, sniffMime(raw))
                        },
                    )
                } else {
                    onResult(null)
                }
            }
        }

        override fun captureFromCamera() = onResult(null)
    }
}

private fun toJpeg(source: BufferedImage): PickedImage {
    val scaled = scaleDown(source, 1280)
    // JPEG has no alpha channel — draw onto an opaque RGB canvas first.
    val rgb = BufferedImage(scaled.width, scaled.height, BufferedImage.TYPE_INT_RGB)
    rgb.createGraphics().apply { drawImage(scaled, 0, 0, null); dispose() }
    val out = ByteArrayOutputStream()
    ImageIO.write(rgb, "jpg", out)
    return PickedImage(out.toByteArray(), "image/jpeg")
}

private fun scaleDown(img: BufferedImage, max: Int): BufferedImage {
    val longest = maxOf(img.width, img.height)
    if (longest <= max) return img
    val scale = max.toDouble() / longest
    val w = (img.width * scale).toInt().coerceAtLeast(1)
    val h = (img.height * scale).toInt().coerceAtLeast(1)
    val scaled = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    scaled.createGraphics().apply {
        drawImage(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
        dispose()
    }
    return scaled
}
