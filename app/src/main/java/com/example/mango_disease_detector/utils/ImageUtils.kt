package com.example.mango_disease_detector.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun uriToCompressedFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scaled = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file
    }
}
