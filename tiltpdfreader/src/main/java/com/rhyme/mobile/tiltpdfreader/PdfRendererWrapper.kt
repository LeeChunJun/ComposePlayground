package com.rhyme.mobile.tiltpdfreader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

class PdfRendererWrapper(context: Context, uri: Uri) {

    private val fileDescriptor: ParcelFileDescriptor =
        if (uri.toString().startsWith("file:///android_asset/")) {
            val fileName =
                uri.lastPathSegment ?: throw IllegalArgumentException("Invalid asset path")
            val file = File(context.cacheDir, fileName)
            context.assets.open(fileName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } else {
            context.contentResolver.openFileDescriptor(uri, "r")!!
        }

    private val renderer = PdfRenderer(fileDescriptor)

    fun renderPage(index: Int, targetWidth: Int = -1, targetHeight: Int = -1): Bitmap {
        val page = renderer.openPage(index)

        // Determine actual render dimensions
        val bitmapWidth: Int
        val bitmapHeight: Int

        if (targetWidth > 0 && targetHeight > 0) {
            // Use specified target dimensions
            bitmapWidth = targetWidth
            bitmapHeight = targetHeight
        } else {
            // Fall back to original page dimensions
            bitmapWidth = page.width
            bitmapHeight = page.height
        }

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }


    fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}