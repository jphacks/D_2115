package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ChooseRole : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        val btnShooting : Button = findViewById(R.id.btnShooting)
        val btnBeingShot : Button = findViewById(R.id.btnBeingShot)
        val btnClose : Button = findViewById(R.id.btnClose)

        btnShooting.setOnClickListener {
            val intent = Intent(this, Shooting::class.java)
            startActivity(intent)
        }

        btnBeingShot.setOnClickListener {
            val intent = Intent(this, BeingShot::class.java)
            startActivity(intent)
        }
        
        btnClose.setOnClickListener {
            finish()
        }
    }




}