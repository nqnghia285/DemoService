package com.nqnghia.demoservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private static final String TAG = "MainActivity";
    private static final String TOPIC1 = "Application_Channel";
    private LocalService.LocalBinder mLocalBinder;
    private volatile static int k;

    private Timer aTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean result = bindService(
            new Intent(MainActivity.this, LocalService.class),
            this,
            Context.BIND_AUTO_CREATE
        );

        Log.d(TAG, "bindService result = " + result);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof LocalService.LocalBinder) {
            mLocalBinder = (LocalService.LocalBinder)iBinder;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        k = 0;
    }

    @Override
    public void onBackPressed() {
        k++;

        if (k < 2) {
            Toast.makeText(this, "Nhấn Back lần nữa để thoát", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    k = 0;
                }
            }, 2000);
        } else {
            mLocalBinder.stopService();

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
        }
    }
}
