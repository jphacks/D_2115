package com.totte


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.totte.databinding.ActivityMain2Binding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.Application
import java.nio.file.Files
import java.nio.file.Paths
import android.view.View

class MyApp :Application(){
    var imageInputStream: InputStream? = null

    companion object {
        private var instance : MyApp? = null
        fun  getInstance(): MyApp {
            if (instance == null)
                instance = MyApp()
            return instance!!
        }
    }
}

class MainActivity2 : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 10
        const val CAMERA_PERMISSION_REQUEST_CODE = 20
    }

    private lateinit var sendImagePath: String
    private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    private lateinit var connectionsClient: ConnectionsClient
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private lateinit var myName: String
    private lateinit var binding: ActivityMain2Binding

    private lateinit var myFirebaseID: String
    private var opponentFirebaseID: String? = null
    private lateinit var dbName: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var db: FirebaseFirestore

    private var allMessages = ArrayList<List<String?>>()

    private lateinit var searchingMessage :TextView
    private lateinit var progressBar :ProgressBar

    // 戻るボタン無効化
    override fun onBackPressed() {}

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // val cameraImage : ImageView = findViewById(R.id.cameraImage)
                val payloadStream: Payload.Stream = payload.asStream()!!
                val payloadInputStream = payloadStream.asInputStream()
                previewImage(payloadInputStream)
                // val bitmap = BitmapFactory.decodeStream(payloadInputStream)
                // cameraImage.setImageBitmap(bitmap)
            }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accepting a connection means you want to receive messages. Hence, the API expects
            // that you attach a PayloadCall to the acceptance
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = "お相手のID：${info.endpointName}"
            opponentFirebaseID = info.endpointName
            defineDB()
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                Snackbar.make(findViewById(R.id.layoutMain2), "見つかりました！！！", Snackbar.LENGTH_SHORT).show()
            }
        }

        override fun onDisconnected(endpointId: String) {
        }
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myFirebaseID,//myName,
            packageName,
            connectionLifecycleCallback,
            options
        )
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myFirebaseID, endpointId, connectionLifecycleCallback)
            // println("Found!!")
            Snackbar.make(findViewById(R.id.layoutMain2), "見つかりました！！！", Snackbar.LENGTH_SHORT).show()
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }

    private fun previewImage(image : InputStream) {
        val myApp = MyApp.getInstance()
        myApp.imageInputStream = image
        val intent = Intent(this, savePicture::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        connectionsClient = Nearby.getConnectionsClient(this)
        myFirebaseID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        searchingMessage = findViewById(R.id.searchingMessage)
        progressBar = findViewById(R.id.progressBar)
        searchingMessage.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        startAdvertising()
        startDiscovery()

        val btnShooting : Button = findViewById(R.id.btnShooting)
        val btnClose : Button = findViewById(R.id.btnClose)

        // val myId : TextView = findViewById(R.id.myId)
        // val destEmailAddrEdit : EditText = findViewById(R.id.destEmailAddrEdit)

        btnShooting.setOnClickListener {
            if (opponentEndpointId != null) {
                if (checkCameraPermission()) {
                    shootPicture()
                } else {
                    grantCameraPermission()
                }
            } else {
                Snackbar.make(findViewById(R.id.layoutMain2), "まだ相手がいないよ…", Snackbar.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener {
            connectionsClient.apply {
                stopAdvertising()
                stopDiscovery()
                stopAllEndpoints()
            }
            finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    @CallSuper
    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
    }

    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }

        if (requestCode == MainActivity2.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shootPicture()
            }
        }
    }
    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName,endpointDiscoveryCallback,options)
    }

    private fun checkCameraPermission() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

    private fun grantCameraPermission() = ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
        Shooting.CAMERA_PERMISSION_REQUEST_CODE
    )

    private fun shootPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(MediaStore.EXTRA_OUTPUT, createSaveFileUri())
        }

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun createSaveFileUri(): Uri {
        println("Called: createSaveFileUri()")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
        val imageFileName = "tmp_$timeStamp"

        val file = File(
            filesDir,
            "$imageFileName.jpg"
        )

        file.createNewFile()

        sendImagePath = file.absolutePath

        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
    }

    private fun goShooting() {
        println("Called: fun goShooting()")
        val intent = Intent(this, Shooting::class.java)
        val requestCode = 1001
        startActivityForResult(intent, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        println("Called: fun onActivityResult")
        println(requestCode)
        println(resultCode)
        super.onActivityResult(requestCode, resultCode, intent)

        /*
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                val sendImagePath = intent?.getStringExtra("KEY",)
                println(sendImagePath)
                val sendImageStream = FileInputStream(File(sendImagePath))
                // println(sendImage?.size)
                // connectionsClient.sendPayload(opponentEndpointId!!, Payload.fromStream(sendImageStream))

                // debugのため手元で表示する
                val cameraImage : ImageView = findViewById(R.id.cameraImage)
                val payloadStream: Payload.Stream = Payload.fromStream(sendImageStream).asStream()!!
                val payloadInputStream = payloadStream.asInputStream()
                val bitmap = BitmapFactory.decodeStream(payloadInputStream)
                cameraImage.setImageBitmap(bitmap)
            }
        }
        */

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val sendImageStream = FileInputStream(File(sendImagePath))
            connectionsClient.sendPayload(opponentEndpointId!!, Payload.fromStream(sendImageStream))
            try {
                Files.deleteIfExists(Paths.get(sendImagePath))
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
            "sender" to myFirebaseID,
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
    private fun defineDB(){
        val ids = listOf(myFirebaseID, opponentFirebaseID!!)
        val ordered_ids = ids.sorted()
        val send : Button = findViewById(R.id.send)
        val messageEdit : EditText = findViewById(R.id.messageEdit)
        //val allMessages = ArrayList<List<String?>>()
        val allMessages = ArrayList<Pair<String, Boolean>>()

        //val searchingMessage : TextView = findViewById(R.id.searchingMessage)
        //val progressBar : ProgressBar = findViewById(R.id.progressBar)
        searchingMessage.visibility = View.GONE
        progressBar.visibility = View.GONE

        viewManager = LinearLayoutManager(this@MainActivity2, LinearLayoutManager.VERTICAL, true)
        viewAdapter = MyAdapter(allMessages)

        dbName = ordered_ids[0] + ordered_ids[1]
        Log.d("TAG", "DBNAME: $dbName")
        db = FirebaseFirestore.getInstance()
        db.collection("messages")
            .document(dbName)
            .collection("inbox")
            .orderBy("datetime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val message = document.getString("message")
                    val sender = document.getString("sender")
                    val isMyMessage = (sender == myFirebaseID)
                    allMessages.add(Pair(message, isMyMessage) as Pair<String, Boolean>)
                }

                viewManager = LinearLayoutManager(this@MainActivity2, LinearLayoutManager.VERTICAL, true)
                viewAdapter = MyAdapter(allMessages)
                recyclerView = binding.messageInbox.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

        //　Firestore更新時の操作の登録
        db.collection("messages")
            .document(dbName)
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
                    val isMyMessage = (sender == myFirebaseID)
                    allMessages.add(Pair(message, isMyMessage) as Pair<String, Boolean>)
                    //allMessages.add(listOf(message, sender))
                }

                // RecyclerViewの更新
                viewAdapter.notifyDataSetChanged()
            }


        //送信ボタン押下時の設定
        send.setOnClickListener {
            sendMessage(dbName, messageEdit.text.toString())
        }
    }
}