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

    fun renderPage(index: Int): Bitmap {
        val page = renderer.openPage(index)
        val bitmap = createBitmap(page.width, page.height)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }

    fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}