package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.app.Activity
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable










class ChooseRole : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        val btnShooting : Button = findViewById(R.id.btnShooting)
        val btnBeingShot : Button = findViewById(R.id.btnBeingShot)
        val btnClose : Button = findViewById(R.id.btnClose)
        val btnSavePicture : Button = findViewById(R.id.btnSavePicture)

        btnShooting.setOnClickListener {
            goShooting()
        }

        btnSavePicture.setOnClickListener {
            val targetImage : ImageView = findViewById(R.id.cameraImage)
            val targetBitmap : Bitmap = (targetImage.drawable as BitmapDrawable).bitmap
            saveToPublish(targetBitmap, "sample.jpeg")
        }

        btnBeingShot.setOnClickListener {
            val intent = Intent(this, BeingShot::class.java)
            startActivity(intent)
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
                val tmp = intent?.getByteArrayExtra("KEY")

                val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp?.size!!)
                cameraImage.setImageBitmap(bitmap)
            }
        }
    }



}