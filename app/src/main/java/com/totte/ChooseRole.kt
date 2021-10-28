package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.app.Activity
import android.content.ContentValues
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ChooseRole : AppCompatActivity() {

    /*
    private val mPayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    // バイト配列を受け取った時
                    val cameraImage : ImageView = findViewById(R.id.cameraImage)
                    val tmp = payload.asBytes()!!
                    val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp?.size!!)
                    cameraImage.setImageBitmap(bitmap)
                }
            }
        }

    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        val btnShooting : Button = findViewById(R.id.btnShooting)
        val btnClose : Button = findViewById(R.id.btnClose)
        val btnSavePicture : Button = findViewById(R.id.btnSavePicture)

        btnShooting.setOnClickListener {
            goShooting()
        }

        btnSavePicture.setOnClickListener {
            val targetImage : ImageView = findViewById(R.id.cameraImage)
            val targetBitmap : Bitmap = (targetImage.drawable as BitmapDrawable).bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
            val fileName = "totte$timeStamp.jpeg"
            saveToPublish(targetBitmap, fileName)
        }
        
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun goShooting() {
        val intent = Intent(this, Shooting::class.java)
        val requestCode = 1001
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                val cameraImage : ImageView = findViewById(R.id.cameraImage)
                val sendImageByte = intent?.getByteArrayExtra("KEY")

                // println(sendImageByte?.size!!)
                // バイト配列を送信する
                // connectionsClient.sendPayload(opponentEndpointId!!, Payload.fromBytes(sendImageByte))


            }
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