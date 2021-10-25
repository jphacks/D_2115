package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Calling : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        val btnGoShooting : Button = findViewById(R.id.btnGoShooting)

        btnGoShooting.setOnClickListener {
            val intent = Intent(this, ChooseRole::class.java)
            startActivity(intent)
            finish()
        }
    }

}