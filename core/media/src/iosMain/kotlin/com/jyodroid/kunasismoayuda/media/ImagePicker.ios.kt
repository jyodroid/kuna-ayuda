package com.jyodroid.kunasismoayuda.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker =
    remember { IosImagePicker(onResult) }

@OptIn(ExperimentalForeignApi::class)
private class IosImagePicker(private val onResult: (PickedImage?) -> Unit) : ImagePicker {

    // Strong ref so the delegate isn't collected while the picker is on screen.
    private var delegate: PickerDelegate? = null

    override val cameraAvailable: Boolean
        get() = UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
        )

    override fun pickFromGallery() =
        present(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)

    override fun captureFromCamera() =
        present(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    private fun present(source: UIImagePickerControllerSourceType) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: run {
            onResult(null); return
        }
        val picker = UIImagePickerController()
        picker.sourceType = source
        val d = PickerDelegate(onResult) { picker.dismissViewControllerAnimated(true, null) }
        delegate = d
        picker.delegate = d
        root.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate(
    private val onResult: (PickedImage?) -> Unit,
    private val dismiss: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        dismiss()
        onResult(image?.let { it.toPickedImage() })
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        dismiss()
        onResult(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toPickedImage(): PickedImage? {
    val resized = resizedTo(1280.0)
    val data = UIImageJPEGRepresentation(resized, 0.8) ?: return null
    return PickedImage(data.toByteArray(), "image/jpeg")
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.resizedTo(maxDim: Double): UIImage {
    val (w, h) = size.useContents { width to height }
    val scale = minOf(maxDim / w, maxDim / h, 1.0)
    if (scale >= 1.0) return this
    val newW = w * scale
    val newH = h * scale
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newW, newH), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, newW, newH))
    val out = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return out ?: this
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
    }
    return bytes
}
