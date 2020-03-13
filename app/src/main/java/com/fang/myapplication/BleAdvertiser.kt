package com.fang.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.nio.ByteBuffer

class BleAdvertiser(
        private val wm: WifiManager
) {
    private val TAG = this.javaClass.name

    private var advertiser: BluetoothLeAdvertiser? = null
    
    fun start() {
        val ip = wm.connectionInfo.ipAddress
        val bytes = ByteBuffer.allocate(4).putInt(ip).array()
//        val bytes = byteArrayOf(102.toByte(), 55.toByte(), 168.toByte(), 192.toByte())
        Log.i(TAG, "------ Listening on ${bytes[3]}.${bytes[2]}.${bytes[1]}.${bytes[0]}")

        advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

        val builder = AdvertiseSettings.Builder()
        val advertiseSettings = builder
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()
        val builder2 = AdvertiseData.Builder()
        builder2.addManufacturerData(0x004c, byteArrayOf(0x09, 0x06, 0x03, 0x05, bytes[3], bytes[2], bytes[1], bytes[0]))
        val advertiseData2 = builder2.build()
        advertiser!!.startAdvertising(advertiseSettings, advertiseData2, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i(TAG, "Began advertising")
            }

            override fun onStartFailure(errorCode: Int) {
                throw RuntimeException(String.format("Error advertising: $errorCode"))
            }
        })
    }
}