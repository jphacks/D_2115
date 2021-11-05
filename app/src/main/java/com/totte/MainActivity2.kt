package com.totte


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import com.totte.databinding.ActivityMain2Binding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity() {

    private val STRATEGY = Strategy.P2P_STAR
    private lateinit var connectionsClient: ConnectionsClient
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private lateinit var myName: String
    private lateinit var binding: ActivityMain2Binding

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
                val cameraImage : ImageView = findViewById(R.id.cameraImage)
                val payloadStream: Payload.Stream = payload.asStream()!!
                val payloadInputStream = payloadStream.asInputStream()
                val bitmap = BitmapFactory.decodeStream(payloadInputStream)
                cameraImage.setImageBitmap(bitmap)
            }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accepting a connection means you want to receive messages. Hence, the API expects
            // that you attach a PayloadCall to the acceptance
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = "お相手の特徴：${info.endpointName}"
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                binding.opponentName.text = opponentName
                Snackbar.make(findViewById(R.id.layoutMain2), "見つかりました！！！", Snackbar.LENGTH_SHORT).show()
            }
        }

        override fun onDisconnected(endpointId: String) {
        }
    }
    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            myName,
            packageName,
            connectionLifecycleCallback,
            options
        )
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myName, endpointId, connectionLifecycleCallback)
            // println("Found!!")
            Snackbar.make(findViewById(R.id.layoutMain2), "見つかりました！！！", Snackbar.LENGTH_SHORT).show()
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        connectionsClient = Nearby.getConnectionsClient(this)
        myName = intent.getStringExtra("NAME").toString()
        startAdvertising()
        startDiscovery()

        val btnShooting : Button = findViewById(R.id.btnShooting)
        val btnClose : Button = findViewById(R.id.btnClose)
        val btnSavePicture : Button = findViewById(R.id.btnSavePicture)

        btnShooting.setOnClickListener {
            /* デバッグ用
            if (opponentEndpointId != null) {
                goShooting()
            } else {
                Snackbar.make(findViewById(R.id.layoutMain2), "まだ相手がいないよ…", Snackbar.LENGTH_SHORT).show()
            }
            */
            goShooting()

        }

        btnClose.setOnClickListener {
            connectionsClient.apply {
                stopAdvertising()
                stopDiscovery()
                stopAllEndpoints()
            }
            finish()
        }

        btnSavePicture.setOnClickListener {
            val targetImage : ImageView = findViewById(R.id.cameraImage)
            val targetBitmap : Bitmap = (targetImage.drawable as BitmapDrawable).bitmap
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
            val fileName = "totte$timeStamp.jpeg"
            saveToPublish(targetBitmap, fileName)
            Snackbar.make(findViewById(R.id.layoutMain2), "保存完了", Snackbar.LENGTH_SHORT).show()
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
    }
    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName,endpointDiscoveryCallback,options)
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
                val sendImagePath = intent?.getStringExtra("KEY",)
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
}