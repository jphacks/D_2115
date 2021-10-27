package com.totte

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.*
import android.R.attr.bitmap
import java.nio.ByteBuffer
import android.R.attr.bitmap
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory





class Shooting : AppCompatActivity() {
    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
        var currentPhotoPath: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shooting)
    }

    override fun onResume() {
        super.onResume()

        if (checkCameraPermission()) {
            shootPicture()
        } else {
            grantCameraPermission()
        }
    }

    private fun checkCameraPermission() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

    private fun grantCameraPermission() = ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shootPicture()
            }
        }
    }

    private fun shootPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.extras?.get("data").let { it ->
                val baos: ByteArrayOutputStream = ByteArrayOutputStream()
                (it as Bitmap).compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val jpgarr: ByteArray = baos.toByteArray()

                intent.putExtra("KEY", jpgarr)
                setResult(Activity.RESULT_OK, intent)
            }
        }

        finish()
    }

}