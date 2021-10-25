package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val btnCancel : Button = findViewById(R.id.btnCancel)

        btnCancel.setOnClickListener {
            finish()
        }

        // for debug
        val btnConnected : Button = findViewById(R.id.btnConnected)

        btnConnected.setOnClickListener {
            val intent = Intent(this, Calling::class.java)
            startActivity(intent)
            finish()
        }
    }
}