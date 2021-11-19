package com.totte

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileOutputStream
import java.io.InputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class savePicture : AppCompatActivity() {

    private lateinit var dbName :String
    private lateinit var myFirebaseID :String

    // 戻るボタン無効化
    override fun onBackPressed() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_picture)

        dbName = intent.getStringExtra("DBNAME").toString()
        myFirebaseID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val cameraImage : ImageView = findViewById(R.id.cameraImage)
        val myApp = MyApp.getInstance()
        val imageInputStream = myApp.imageInputStream
        val btnSavePicture : Button = findViewById(R.id.btnSavePicture)
        val btnDisposePicture : Button = findViewById(R.id.btnDisposePicture)

        val bitmap = BitmapFactory.decodeStream(imageInputStream)
        cameraImage.setImageBitmap(bitmap)

        btnSavePicture.setOnClickListener {
            val targetImage : ImageView = findViewById(R.id.cameraImage)
            val targetBitmap : Bitmap = (targetImage.drawable as BitmapDrawable).bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
            val fileName = "totte$timeStamp.jpeg"
            saveToPublish(targetBitmap, fileName)
            // Snackbar.make(findViewById(R.id.layoutSave), "保存完了", Snackbar.LENGTH_SHORT).show()
            sendMessage(dbName, "画像を保存しました!!")
            finish()
        }

        btnDisposePicture.setOnClickListener {
            sendMessage(dbName, "画像を破棄しました")
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

    fun sendMessage(destEmailAddr: String, message: String) {
        Log.d("Firestore", "send message : $message")
        val db = FirebaseFirestore.getInstance()

        // 現在時刻の取得
        val date = Date()
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        val mail = hashMapOf(
            "datetime" to format.format(date),
            "sender" to myFirebaseID,
            "message" to message
        )

        db.collection("messages")
            .document(destEmailAddr)
            .collection("inbox")
            .add(mail)
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }
    }

}