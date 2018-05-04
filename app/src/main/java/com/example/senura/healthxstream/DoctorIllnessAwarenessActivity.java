package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DoctorIllnessAwarenessActivity extends AppCompatActivity implements MqttCallback {

    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    private String clientID=null;
    private String did=null;
    private String docName=null;

    private String jsonResponse = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_illness_awareness);

        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");

        client=clientTemp;
        setClientListenToThisAct();
    }

    private void setClientListenToThisAct(){
        client.setCallback(DoctorIllnessAwarenessActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
