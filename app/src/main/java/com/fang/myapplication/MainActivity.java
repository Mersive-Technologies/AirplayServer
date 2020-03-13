package com.fang.myapplication;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apple.dnssd.TXTRecord;

public class MainActivity extends Activity implements View.OnClickListener {

    public static String TAG = "MainActivity";

    private AirPlayServer mAirPlayServer;
    private RaopServer mRaopServer;
    private DNSNotify mDNSNotify;
    private BleAdvertiser mAdvertiser;

    private SurfaceView mSurfaceView;
    private Button mBtnControl;
    private TextView mTxtDevice;
    private boolean mIsStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSystemService(Context.NSD_SERVICE);
        mBtnControl = findViewById(R.id.btn_control);
        mTxtDevice = findViewById(R.id.txt_device);
        mBtnControl.setOnClickListener(this);
        mSurfaceView = findViewById(R.id.surface);
        mAirPlayServer = new AirPlayServer();
        mRaopServer = new RaopServer(mSurfaceView);
        mDNSNotify = new DNSNotify();

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mAdvertiser = new BleAdvertiser(wm);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_control: {
                if (!mIsStart) {
                    startServer();
                    mTxtDevice.setText("Device name:");// + mDNSNotify.getDeviceName());
                } else {
                    stopServer();
                    mTxtDevice.setText("not initiated");
                }
                mIsStart = !mIsStart;
                mBtnControl.setText(mIsStart ? "End" : "Start");
                break;
            }
        }
    }

    private void startServer() {
        mAdvertiser.start();
        mDNSNotify.changeDeviceName();
        mAirPlayServer.startServer();
        TXTRecord airplayRecord = null;
        TXTRecord raopRecord = null;
        int airplayPort = mAirPlayServer.getPort();
        if (airplayPort == 0) {
            Toast.makeText(this.getApplicationContext(), "Failed to start airplay service", Toast.LENGTH_SHORT).show();
        } else {
            airplayRecord = mDNSNotify.registerAirplay(airplayPort);
        }
        mRaopServer.startServer();
        int raopPort = mRaopServer.getPort();
        if (raopPort == 0) {
            Toast.makeText(this.getApplicationContext(), "Failed to start raop service", Toast.LENGTH_SHORT).show();
        } else {
            raopRecord = mDNSNotify.registerRaop(raopPort);
        }
        if(airplayRecord != null) {
            mRaopServer.setAirplayRecord(airplayRecord.getRawBytes());
        }
        if(raopRecord != null) {
            mRaopServer.setRaopRecord(raopRecord.getRawBytes());
        }
        Log.d(TAG, "airplayPort = " + airplayPort + ", raopPort = " + raopPort);
    }

    private void stopServer() {
        mDNSNotify.stop();
        mAirPlayServer.stopServer();
        mRaopServer.stopServer();
    }

}


