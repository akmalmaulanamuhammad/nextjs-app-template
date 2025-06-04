package com.example.absensi.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    private const val JPEG_QUALITY = 80
    private const val MAX_IMAGE_DIMENSION = 1024
    private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    private const val JPEG_PREFIX = "JPEG_"
    private const val JPEG_EXTENSION = ".jpg"

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${JPEG_PREFIX}${timeStamp}_",
            JPEG_EXTENSION,
            storageDir
        )
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun compressImage(context: Context, sourceFile: File): File? {
        return try {
            // Read bitmap with correct orientation
            val bitmap = getBitmapFromFile(sourceFile)
            
            // Create a new file for the compressed image
            val compressedFile = createImageFile(context)
            
            // Compress and save the bitmap
            FileOutputStream(compressedFile).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            
            bitmap?.recycle()
            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapFromFile(sourceFile: File): Bitmap? {
        return try {
            // Get bitmap dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options)
            options.inJustDecodeBounds = false

            // Decode bitmap with sample size
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // Rotate bitmap if needed
            val rotation = getImageRotation(sourceFile)
            if (rotation != 0f && bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(rotation)
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.width, bitmap.height,
                    matrix, true
                )
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > MAX_IMAGE_DIMENSION || width > MAX_IMAGE_DIMENSION) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION ||
                (halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun getImageRotation(imageFile: File): Float {
        return try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            0f
        }
    }

    fun deleteImage(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun clearImageCache(context: Context) {
        val imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageDir?.listFiles()?.forEach { file ->
            if (file.name.startsWith(JPEG_PREFIX)) {
                file.delete()
            }
        }
    }

    fun isImageFile(file: File): Boolean {
        return file.exists() && file.extension.lowercase() in arrayOf("jpg", "jpeg", "png")
    }

    fun getImageSize(file: File): Long {
        return if (file.exists()) file.length() else 0
    }

    fun getImageDimensions(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }

    fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        return when {
            kb < 1024 -> String.format("%.1f KB", kb)
            else -> String.format("%.1f MB", kb / 1024)
        }
    }

    fun getImageInfo(file: File): ImageInfo {
        val size = getImageSize(file)
        val dimensions = getImageDimensions(file)
        return ImageInfo(
            width = dimensions.first,
            height = dimensions.second,
            size = size,
            formattedSize = formatFileSize(size),
            rotation = getImageRotation(file)
        )
    }

    data class ImageInfo(
        val width: Int,
        val height: Int,
        val size: Long,
        val formattedSize: String,
        val rotation: Float
    )
}
