package com.totte

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main2.*
import android.location.LocationManager
import android.view.View
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.core.app.ActivityCompat

class MainActivity2 : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private val MY_REQUEST_CODE = 2
    private val REQUEST_CODE_LOCATION = 100
    private lateinit var adapter: BluetoothAdapter
    private var mScanning: Boolean = false
    private var isGpsEnabled: Boolean = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                // 主にデバイスを見つけた時
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device == null) {
                        Log.d("nullDevice", "Device is null")
                        return
                    }

                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    val deviceUUID = device?.uuids
                    val deviceRssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    Log.d("device", "Device name: ${deviceName}, address:${deviceHardwareAddress}, UUID:${deviceUUID}, RSSI:${deviceRssi}")

                    // MainActivityのScrollViewに検知したデバイスのデータを表示
                    val textView = TextView(context)
                    textView.text = "Device name: ${deviceName}\naddress:${deviceHardwareAddress}, UUID:${deviceUUID}, RSSI:${deviceRssi}"
                    device_num_list.addView(textView)

                    return
                }
                // 検知を開始した時
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Toast.makeText(
                            context,
                            "Bluetooth検出開始",
                            Toast.LENGTH_SHORT
                    ).show()
                    Log.d("discoveryStart", "Discovery Started")
                    mScanning = true
                    return
                }
                // 検知が終了した時
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { //cancelDiscoveryでも呼ばれる
                    mScanning = false

                    Toast.makeText(
                            context,
                            "Bluetooth検出終了",
                            Toast.LENGTH_SHORT
                    ).show()
                    Log.d("discoveryFinish", "Discovery finished")
                }
            }
        }
    }
    //private val listView: ListView? = null
    //private val deviceList: DeviceList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val btnCancel : Button = findViewById(R.id.btnCancel)

        var bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.getAdapter()//getDefaultAdapter()

        if (adapter == null){
            finish()
        }
//
//        if (adapter?.isEnabled == false) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }

        //var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnabled = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun requestLocationFeature() {
        if (isGpsEnabled) {
            Log.d("GPS", "有効")
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    private fun requestBluetoothFeature() {
        if (adapter.isEnabled) {
            return
        }

        // Bluetoothの有効化要求
        var enableBluetoothIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(
                enableBluetoothIntent,
                REQUEST_ENABLE_BT
        )
    }

    override fun onResume(){
        super.onResume()
        requestLocationFeature()
        requestBluetoothFeature()
        adapter.startDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            RESULT_OK ->{
                Toast.makeText(applicationContext, "bluetooth enabled", Toast.LENGTH_LONG).show()
                //Toast.makeText(applicationContext, R.string.valid, Toast.LENGTH_LONG).show()
                //Log.d("qwert", "有効")
            }
            RESULT_CANCELED ->{
                Toast.makeText(applicationContext, "bluetooth disabled", Toast.LENGTH_LONG).show()
                finish()
                //Toast.makeText(applicationContext, R.string.not_valid, Toast.LENGTH_LONG).show()
                //Log.d("qwert", "無効")
            }
        }
    }


}