package com.nqnghia.demoservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

public class LocalService extends Service {
    private static final String TAG = "LocalService";
    // Parameter CloudMQTT
    private static final String TOPIC1 = "Application_Channel";
    private static final String TOPIC2 = "Lights_Channel";
    private static final int QoS0 = 0;
    private static final int QoS2 = 2;
    private static final boolean retained = true;
    private static final int PORT = 15596;
    private static final String SERVER = "m11.cloudmqtt.com";
    private static final String USER = "wvtkpmil";
    private static final String PASSWORD = "PqCp38yh_Wnz";

    private MqttHelper mqttHelper;

    private volatile static Boolean StartedFlag;

    private final LocalBinder mLocalBinder = new LocalBinder();

    private static final Timer mTimer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public class LocalBinder extends Binder {

        public LocalBinder() {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mLocalBinder != null) {
                        mLocalBinder.send(TOPIC1, "Ahihi");
                    }
                }
            }, 3000, 3000);
        }

        public LocalService getService() {
            return LocalService.this;
        }

        public void send(String topic, String message) {
            if (mqttHelper != null) {
                mqttHelper.publish(topic, message.getBytes());
            }
        }

        public void send(String topic, String message, int qos) {
            if (mqttHelper != null) {
                mqttHelper.publish(topic, message.getBytes(), qos);
            }
        }

        public void subscribe(String topic, int qos) {
            if (mqttHelper != null) {
                mqttHelper.subscribe(topic, qos);
            }
        }

        public void unsubscribe(String topic) {
            if (mqttHelper != null) {
                mqttHelper.unsubscribe(topic);
            }
        }

        public void connect() {
            mqttHelper.connect();
        }

        public void disconnect() {
            mqttHelper.disconnect();
        }

        public boolean isConnected() {
            return mqttHelper.isConnected();
        }

        public void stopService() {
            LocalService.this.stopSelf();
        }
    }

    @Override
    public void onCreate() {
        StartedFlag = false;
        mqttHelper = new MqttHelper(LocalService.this, SERVER, PORT, USER, PASSWORD);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Looper.prepare();

                if (mqttHelper != null) {
                    mqttHelper.connect();

                    mqttHelper.setMqttHandler(new MqttHelper.MqttHandler() {
                        @Override
                        public void handle(String topic, MqttMessage message) {
                            if (StartedFlag) {
                                Log.d(topic, message.toString());
                            } else {
                                StartedFlag = true;
                            }
                        }
                    });

                    mqttHelper.setMqttSubscribe(new MqttHelper.MqttSubscribe() {
                        @Override
                        public void setSubscribe(IMqttToken asyncActionToken) {
                            mqttHelper.subscribe(TOPIC1, QoS0);
                            mqttHelper.addTopic(TOPIC1);
                        }
                    });
                    Log.d("XXX", "Connected");
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        mTimer.purge();
        mTimer.cancel();
        mqttHelper.destroy();
        LocalService.this.stopSelf();
        super.onDestroy();
    }
}
