package com.rhyme.mobile.tiltpdfreader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.createBitmap

class PdfRendererWrapper(context: Context, uri: Uri) {

    private val fileDescriptor: ParcelFileDescriptor =
        context.contentResolver.openFileDescriptor(uri, "r")!!

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