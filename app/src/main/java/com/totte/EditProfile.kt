package com.totte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.totte.databinding.ActivityEditProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class EditProfile : AppCompatActivity() {
    private lateinit var myFirebaseID :String
    private lateinit var dbName :String
    private lateinit var db :FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        val btnSaveProfile : Button = findViewById(R.id.saveProfile)
        val greeting : EditText = findViewById(R.id.greeting)
        var template_greeting :String? = ""

        myFirebaseID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dbName = myFirebaseID

        btnSaveProfile.setOnClickListener{
            saveProfile(dbName, greeting.text.toString())
        }

        db = FirebaseFirestore.getInstance()
        db.collection("greeting")
            .document(myFirebaseID)
            .collection("inbox")
            .orderBy("datetime", Query.Direction.DESCENDING)
            .limit(2)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    template_greeting = document.getString("message")
                    break
                }
                if (template_greeting != "")
                    greeting.setHint(template_greeting)
            }

    }

    fun saveProfile(destEmailAddr: String, message: String) {
        val db = FirebaseFirestore.getInstance()
        val messageEdit : EditText = findViewById(R.id.greeting)

        // 現在時刻の取得
        val date = Date()
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        val mail = hashMapOf(
            "datetime" to format.format(date),
            "sender" to myFirebaseID,
            "message" to message
        )

        db.collection("greeting")
            .document(destEmailAddr)
            .collection("inbox")
            .add(mail)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "保存完了！", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }
    }
}