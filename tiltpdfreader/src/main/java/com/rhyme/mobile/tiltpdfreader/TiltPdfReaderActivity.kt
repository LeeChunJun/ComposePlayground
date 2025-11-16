package com.rhyme.mobile.tiltpdfreader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.net.toUri
import java.io.FileNotFoundException

class TiltPdfReaderActivity : AppCompatActivity(),
    TextureView.SurfaceTextureListener {

    private lateinit var textureView: TextureView
    private lateinit var pdfRenderer: PdfRendererWrapper
    private lateinit var viewportUpdater: ViewportUpdater
    private lateinit var tiltSensor: TiltSensorManager

    /* 用来缓存当前页 Bitmap */
    private var currentBitmap: Bitmap? = null

    private val pdfUri: Uri by lazy {
        // 把 sample.pdf 放进 res/raw/
        "android.resource://$packageName/raw/ooc.pdf".toUri()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tilt_pdf_reader)

        textureView = findViewById(R.id.pdfTextureView)
        textureView.surfaceTextureListener = this
    }

    /* ---------- SurfaceTextureListener ---------- */
    override fun onSurfaceTextureAvailable(
        surface: SurfaceTexture, width: Int, height: Int
    ) {
        pdfRenderer = PdfRendererWrapper(this, pdfUri)
        viewportUpdater = ViewportUpdater(textureView)
        tiltSensor = TiltSensorManager(this) { p, r ->
            viewportUpdater.updateOffset(p, r)
        }

        try {
            currentBitmap = pdfRenderer.renderPage(0)
            drawBitmap()
            tiltSensor.start()
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "PDF file not found: ooc.pdf", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture, width: Int, height: Int
    ) = Unit

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        tiltSensor.stop()
        pdfRenderer.close()
        currentBitmap?.recycle()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
    /* -------------------------------------------- */

    /* 把 Bitmap 画到 TextureView 当前画布上 */
    private fun drawBitmap() {
        val bitmap = currentBitmap ?: return
        var canvas: Canvas? = null
        try {
            canvas = textureView.lockCanvas()
            canvas?.apply {
                drawColor(Color.WHITE)          // 清背景
                drawBitmap(bitmap, 0f, 0f, null)
            }
        } finally {
            canvas?.let { textureView.unlockCanvasAndPost(it) }
        }
    }
}