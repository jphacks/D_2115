package com.totte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    //private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        Log.d("current", "currentUser = ${auth.currentUser?.uid.toString()}")
        signInAnonymously()

        val btnSearchBegin : Button = findViewById(R.id.btnSearchBegin)
        val btnChatBegin : Button = findViewById(R.id.btnChatBegin)
        val name : EditText = findViewById(R.id.name)
        val searchingText : TextView = findViewById(R.id.searchingText)
        val progressBar : ProgressBar = findViewById(R.id.progressBar)

        searchingText.visibility = View.GONE
        progressBar.visibility = View.GONE
        //shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)


        btnSearchBegin.setOnClickListener {

            searchingText.visibility = View.VISIBLE
            btnSearchBegin.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            val intent = Intent(this, MainActivity2::class.java)
            intent.putExtra("NAME",name.text.toString())
            startActivity(intent)
        }

        btnChatBegin.setOnClickListener {
            val intent = Intent(this, Talking::class.java)
            // intent.putExtra("UID", auth.currentUser?.uid.toString())
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()

        val searchingText : TextView = findViewById(R.id.searchingText)
        val btnSearchBegin : Button = findViewById(R.id.btnSearchBegin)
        val progressBar : ProgressBar = findViewById(R.id.progressBar)
        searchingText.visibility = View.GONE
        btnSearchBegin.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("success", "currentUser = ${auth.currentUser?.uid.toString()}")
                } else {
                    Log.d("error", task.exception.toString())
                }
            }
    }
}