package com.totte

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.totte.databinding.ActivityTalkingBinding
import java.text.SimpleDateFormat
import java.util.*

class Talking : AppCompatActivity() {

    private lateinit var binding: ActivityTalkingBinding

    private lateinit var myEmailAddr: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var db: FirebaseFirestore

    private var allMessages = ArrayList<List<String?>>()

    @SuppressLint("WrongViewCast", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTalkingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // val myUserID = intent.getStringExtra("UID")

        myEmailAddr = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val myId : TextView = findViewById(R.id.myId)
        val send : Button = findViewById(R.id.send)
        val messageEdit : EditText = findViewById(R.id.messageEdit)
        val destEmailAddrEdit : EditText = findViewById(R.id.destEmailAddrEdit)

        //自分のユーザー名を表示
        myId.setText(myEmailAddr)

        db = FirebaseFirestore.getInstance()
        val allMessages = ArrayList<List<String?>>()
        db.collection("messages")
            .document(myEmailAddr)
            .collection("inbox")
            .orderBy("datetime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val message = document.getString("message")
                    val sender = document.getString("sender")
                    allMessages.add(listOf(message, sender))
                }

                viewManager = LinearLayoutManager(this)
                viewAdapter = MyAdapter(allMessages)
                recyclerView = binding.messageInbox.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

        //　Firestore更新時の操作の登録
        db.collection("messages")
            .document(myEmailAddr)
            .collection("inbox")
            .orderBy("datetime", Query.Direction.DESCENDING)
            // Firestoreの更新時の操作を登録
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                allMessages.clear()
                for (doc in value!!) {
                    val message = doc.getString("message")
                    val sender = doc.getString("sender")
                    allMessages.add(listOf(message, sender))
                }

                // RecyclerViewの更新
                viewAdapter.notifyDataSetChanged()
            }


        //送信ボタン押下時の設定
        send.setOnClickListener {
            sendMessage(destEmailAddrEdit.text.toString(), messageEdit.text.toString())
        }
    }

    fun sendMessage(destEmailAddr: String, message: String) {
        val db = FirebaseFirestore.getInstance()
        val messageEdit : EditText = findViewById(R.id.messageEdit)

        // 現在時刻の取得
        val date = Date()
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        val mail = hashMapOf(
            "datetime" to format.format(date),
            "sender" to myEmailAddr,
            "message" to message
        )

        db.collection("messages")
            .document(destEmailAddr)
            .collection("inbox")
            .add(mail)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "送信完了！", Toast.LENGTH_LONG).show()
                messageEdit.text.clear()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }
    }

}