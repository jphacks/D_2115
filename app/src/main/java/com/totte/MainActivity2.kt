package com.totte

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
    }
}