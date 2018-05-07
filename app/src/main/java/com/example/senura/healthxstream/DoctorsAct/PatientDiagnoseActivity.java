package com.example.senura.healthxstream.DoctorsAct;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.senura.healthxstream.R;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class PatientDiagnoseActivity extends AppCompatActivity {


    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_diagnose);
    }
}
