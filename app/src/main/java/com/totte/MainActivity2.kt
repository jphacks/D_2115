package com.totte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.totte.databinding.ActivityMain2Binding
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class MainActivity2 : AppCompatActivity() {
    /**
     * Strategy for telling the Nearby Connections API how we want to discover and connect to
     * other nearby devices. A star shaped strategy means we want to discover multiple devices but
     * only connect to and communicate with one at a time.
     */
    private val STRATEGY = Strategy.P2P_STAR

    /**
     * Our handle to the [Nearby Connections API][ConnectionsClient].
     */
    private lateinit var connectionsClient: ConnectionsClient

    /**
     * The request code for verifying our call to [requestPermissions]. Recall that calling
     * [requestPermissions] leads to a callback to [onRequestPermissionsResult]
     */
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    /*
    The following variables are convenient ways of tracking the data of the opponent that we
    choose to play against.
    */
    private var newText : String? = null
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private var myCodeName: String = CodenameGenerator.generate()
    internal object CodenameGenerator {
        private val COLORS = arrayOf(
            "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "Purple", "Lavender"
        )
        private val TREATS = arrayOf(
            "Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb",
            "Ice Cream Sandwich", "Jellybean", "Kit Kat", "Lollipop", "Marshmallow", "Nougat",
            "Oreo", "Pie"
        )
        private val generator = Random()

        /** Generate a random Android agent codename  */
        fun generate(): String {
            val color = COLORS[generator.nextInt(COLORS.size)]
            val treat = TREATS[generator.nextInt(TREATS.size)]
            return "$color $treat"
        }
    }

    /**
     * This is for wiring and interacting with the UI views.
     */
    private lateinit var binding: ActivityMain2Binding

    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                newText = String(it, UTF_8)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accepting a connection means you want to receive messages. Hence, the API expects
            // that you attach a PayloadCall to the acceptance
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = "Opponent\n(${info.endpointName})"
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                call()
            }
        }

        override fun onDisconnected(endpointId: String) {
        }
    }
    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
            myCodeName,
            packageName,
            connectionLifecycleCallback,
            options
        )
    }
    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myCodeName, endpointId, connectionLifecycleCallback)
            println("Found!!")
            call()
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

        startAdvertising()
        startDiscovery()

        val btnCancel : Button = findViewById(R.id.btnCancel)

        btnCancel.setOnClickListener {
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
    }
    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName,endpointDiscoveryCallback,options)
    }
    private fun call(){
        val intent = Intent(this, Calling::class.java)
        startActivity(intent)
    }
}