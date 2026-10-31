package com.zubair.social.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/** Device par chalta hai (BitmapFactory/ExifInterface asli Android chahiye). Apni banayi hui image use karta hai. */
class ImageCompressorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /** Noise wali bari JPEG (quality 95) taake size asli photo jaisa ho, optionally EXIF orientation ke saath. */
    private fun makeJpeg(name: String, w: Int, h: Int, orientation: Int? = null): File {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint()
        val rnd = Random(1)
        repeat(4000) {
            paint.color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val x = rnd.nextInt(w); val y = rnd.nextInt(h)
            canvas.drawRect(x.toFloat(), y.toFloat(), (x + 120).toFloat(), (y + 120).toFloat(), paint)
        }
        val file = File(context.cacheDir, name)
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        bmp.recycle()
        if (orientation != null) {
            ExifInterface(file.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        return file
    }

    private fun decodedSize(bytes: ByteArray): Pair<Int, Int> {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, o)
        return o.outWidth to o.outHeight
    }

    @Test
    fun largeLandscape_isResizedTo1080Wide_andSmaller() = runBlocking {
        val file = makeJpeg("big_landscape.jpg", 4000, 3000)
        val r = ImageCompressor.compress(context, Uri.fromFile(file))

        assertEquals(1080, r.width)
        assertEquals(810, r.height)                       // 4:3 ratio bacha rahe
        assertEquals(r.width to r.height, decodedSize(r.bytes))
        assertTrue("after (${r.bytes.size}) should be < before (${file.length()})", r.bytes.size < file.length())
        assertEquals(file.length(), r.originalSizeBytes)
        assertEquals(4000, r.originalWidth)
    }

    @Test
    fun exifRotate90_isAppliedToPixels() = runBlocking {
        // 4000x3000 stored + "rotate 90": phone mein 3000x4000 portrait dikhti hai -> 1080x1440.
        val file = makeJpeg("rotated.jpg", 4000, 3000, ExifInterface.ORIENTATION_ROTATE_90)
        val r = ImageCompressor.compress(context, Uri.fromFile(file))

        assertEquals(1080, r.width)
        assertEquals(1440, r.height)
        assertEquals(3000, r.originalWidth)               // original bhi "dikhne wali" dimensions mein
        assertEquals(4000, r.originalHeight)
    }

    @Test
    fun smallImage_isNotUpscaled() = runBlocking {
        val file = makeJpeg("small.jpg", 600, 400)
        val r = ImageCompressor.compress(context, Uri.fromFile(file))

        assertEquals(600, r.width)
        assertEquals(400, r.height)
        assertNotNull(r.bytes)
    }

    @Test(expected = java.io.IOException::class)
    fun notAnImage_throws() {
        val file = File(context.cacheDir, "not_image.jpg").apply { writeText("hello") }
        runBlocking { ImageCompressor.compress(context, Uri.fromFile(file)) }
    }
}
