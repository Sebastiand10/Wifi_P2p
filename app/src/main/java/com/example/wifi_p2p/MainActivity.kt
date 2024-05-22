package com.example.wifi_p2p

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wifi_p2p.Recivers.WiFiDirectBroadcastReceiver

class MainActivity : AppCompatActivity(), WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
        discoverPeers()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Discovery Initiated")
            }

            override fun onFailure(reason: Int) {
                Log.d("MainActivity", "Discovery Failed: $reason")
            }
        })
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        peers?.deviceList?.let { deviceList ->
            if (deviceList.isNotEmpty()) {
                val device = deviceList.first()
                connectToDevice(device)
            }
        }
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Connected to device: ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Log.d("MainActivity", "Connection failed: $reason")
            }
        })
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        info?.let {
            Log.d("MainActivity", "Connection Info Available: $info")
            // Handle connection info
        }
    }
}
