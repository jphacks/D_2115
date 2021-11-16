package com.totte

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import java.io.FileOutputStream
import java.io.InputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class savePicture : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_picture)

        val image = intent.getByteArrayExtra("IMAGE")
        val cameraImage : ImageView = findViewById(R.id.cameraImage)
        val bitmap = image?.let { BitmapFactory.decodeByteArray(image, 0, it.size) }
        cameraImage.setImageBitmap(bitmap)

        val btnSavePicture : Button = findViewById(R.id.btnSavePicture)

        btnSavePicture.setOnClickListener {
            val targetImage : ImageView = findViewById(R.id.cameraImage)
            val targetBitmap : Bitmap = (targetImage.drawable as BitmapDrawable).bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
            val fileName = "totte$timeStamp.jpeg"
            saveToPublish(targetBitmap, fileName)
            Snackbar.make(findViewById(R.id.layoutMain2), "保存完了", Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveToPublish(photoBitmap: Bitmap, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToPublishWithContentResolver(photoBitmap, name)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToPublishWithContentResolver(photoBitmap: Bitmap, name: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentResolver = contentResolver
        val item = contentResolver.insert(collection, values)!!

        contentResolver.openFileDescriptor(item, "w", null).use {
            FileOutputStream(it!!.fileDescriptor).use { out ->
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        }
    }
}