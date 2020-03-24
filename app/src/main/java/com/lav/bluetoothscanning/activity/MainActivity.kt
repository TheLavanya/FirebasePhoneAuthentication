package com.lav.bluetoothscanning.activity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lav.bluetoothscanning.R

const val TAG = "BLE_SCAN_TEST"

class MainActivity : AppCompatActivity() {

    val REQUEST_ENABLE_BT = 1001
    val REQUEST_ENABLE_DISCOVERABLE = 1002
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initBLE()
    }

    private fun initBLE() {

        if (!getBluetoothAdapter().isEnabled) {
            startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        }
    }

    override fun onResume() {
        super.onResume()
        registerBluetoothStateReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterBluetoothStateReceiver()
    }

    private fun registerBluetoothStateReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, intentFilter)
    }

    private fun unregisterBluetoothStateReceiver() {
        unregisterReceiver(bluetoothStateReceiver)
    }

    private fun getBluetoothAdapter(): BluetoothAdapter {
        if (bluetoothAdapter != null)
            return bluetoothAdapter!!
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT) {
                startDiscoverable()
            } else if (requestCode == REQUEST_ENABLE_DISCOVERABLE) {
                startScanDevices()
            }
        }
    }

    private fun startDiscoverable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0)
        }
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_DISCOVERABLE)
    }

    private fun startScanDevices() {
        val scanner = getBluetoothAdapter().bluetoothLeScanner
        if (scanner != null) {
            scanner.startScan(initScanCallback())
            Log.d(TAG, "scan started");
        } else Log.e(TAG, "could not get scanner object");
    }

    private fun stopScanDevices() {
        val scanner = getBluetoothAdapter().bluetoothLeScanner
        if (scanner != null) {
            scanner.stopScan(initScanCallback())
            Log.d(TAG, "scan started");
        } else Log.e(TAG, "could not get scanner object");
    }

    private fun initScanCallback(): ScanCallback {
        return object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val state = intent?.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.i(TAG, "Bluetooth State OFF")
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.i(TAG, "Bluetooth State ON")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        Log.i(TAG, "Bluetooth State turning off")
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        Log.i(TAG, "Bluetooth State turning on")
                    }
                }
            }
        }
    }
}