package com.zubair.social.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.roundToInt

/** Upload ke liye tayyar JPEG bytes, saath mein before/after numbers. */
data class CompressedImage(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val originalSizeBytes: Long,   // -1 = pata nahi chala
    val originalWidth: Int,
    val originalHeight: Int,
)

/**
 * Photo Picker ki image ko upload se pehle chhota karta hai: max [maxWidth] px chaudai, JPEG [quality].
 * Phone ki photo 4000x3000 / 5 MB hoti hai; Instagram-jaisi grid ke liye 1080px kaafi hai aur upload tez hota hai.
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"
    const val MAX_WIDTH = 1080
    const val JPEG_QUALITY = 80

    /** Background (Dispatchers.IO) par chalta hai: decode + resize + compress main thread ko block nahi karte. */
    suspend fun compress(
        context: Context,
        uri: Uri,
        maxWidth: Int = MAX_WIDTH,
        quality: Int = JPEG_QUALITY,
    ): CompressedImage = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val startedAt = System.nanoTime()

        val originalSize = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

        // 1) Sirf size parho (pixels decode nahi): 12MP image poori memory mein layne se bachne ke liye.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val srcWidth = bounds.outWidth
        val srcHeight = bounds.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) throw IOException("Couldn't read image: $uri")

        // 2) Decode karte waqt hi chhota karo (power of 2), lekin maxWidth se neeche nahi jao.
        var sample = 1
        while (srcWidth / (sample * 2) >= maxWidth) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IOException("Couldn't decode image: $uri")

        // 3) Camera photos aksar sideways store hoti hain + EXIF "rotate" flag: pixels ko seedha karo,
        //    warna JPEG re-encode ke baad EXIF jata hai aur photo ulti dikhti hai.
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val matrix = Matrix().apply { applyExifOrientation(orientation) }

        // 4) Exact maxWidth par scale (kabhi upscale nahi). Rotation 90/270 ho to dikhne wali chaudai height hoti hai.
        val rotated = orientation in setOf(
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSPOSE, ExifInterface.ORIENTATION_TRANSVERSE,
        )
        val visibleWidth = if (rotated) decoded.height else decoded.width
        if (visibleWidth > maxWidth) {
            val scale = maxWidth.toFloat() / visibleWidth
            matrix.postScale(scale, scale)
        }
        var bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (bitmap !== decoded) decoded.recycle()

        // 5) PNG/transparent image JPEG mein jaye to transparency kali ho jati hai: safed background par rakho.
        if (bitmap.hasAlpha()) {
            val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Canvas(flat).apply { drawColor(Color.WHITE); drawBitmap(bitmap, 0f, 0f, null) }
            bitmap.recycle()
            bitmap = flat
        }

        // 6) JPEG compress.
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        val result = CompressedImage(
            bytes = out.toByteArray(),
            width = bitmap.width,
            height = bitmap.height,
            originalSizeBytes = originalSize,
            originalWidth = if (rotated) srcHeight else srcWidth,
            originalHeight = if (rotated) srcWidth else srcHeight,
        )
        bitmap.recycle()

        logResult(result, (System.nanoTime() - startedAt) / 1_000_000, Thread.currentThread().name)
        result
    }

    private fun logResult(r: CompressedImage, tookMs: Long, thread: String) {
        val after = r.bytes.size.toLong()
        val saved = if (r.originalSizeBytes > 0) (100 - (after * 100.0 / r.originalSizeBytes)).roundToInt() else null
        Log.i(
            TAG,
            "BEFORE ${r.originalWidth}x${r.originalHeight}, ${formatSize(r.originalSizeBytes)}  ->  " +
                "AFTER ${r.width}x${r.height}, ${formatSize(after)}" +
                (saved?.let { "  (saved $it%)" } ?: "") + "  [${tookMs} ms on $thread]",
        )
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 0 -> "unknown size"
        bytes < 1024 * 1024 -> "%.0f KB".format(bytes / 1024.0)
        else -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
    }

    private fun Matrix.applyExifOrientation(orientation: Int) {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
        }
    }
}
