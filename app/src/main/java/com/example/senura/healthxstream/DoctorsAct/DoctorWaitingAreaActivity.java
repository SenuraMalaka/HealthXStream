package com.example.senura.healthxstream.DoctorsAct;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.senura.healthxstream.BodyTemperatureActivity;
import com.example.senura.healthxstream.MainActivity;
import com.example.senura.healthxstream.MyDoctorsActivity;
import com.example.senura.healthxstream.R;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.example.senura.healthxstream.mqttConnectionPackage.uniqueIDgenerator;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DoctorWaitingAreaActivity extends AppCompatActivity implements MqttCallback {

    //MQTT Vars
    private MqttConnection mConnection = new MqttConnection();
    private MqttAndroidClient client = null;
    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;
    String doctorID=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_waiting_area);

        doctorID="345";//should get the docID from the login
        setButtons();

    }


    private void hideSearchTextArea(){
        LinearLayout lL_Search;
        lL_Search = (LinearLayout) findViewById(R.id.linearLayout_DWA_SearchingTextArea);
        lL_Search.setVisibility(View.GONE);
    }

    private void setButtons(){
        Button button_Search;

        //BodyTemp
        button_Search = (Button) findViewById(R.id.button_DWA_Search);
        button_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectMqttClient();//subscribe to topic
                hideSearchTextArea();
            }
        });

    }


    private void connectMqttClient() {
        client = mConnection.connect(DoctorWaitingAreaActivity.this, this, "healthxtream/doctor/"+doctorID, false);
        client.setCallback(DoctorWaitingAreaActivity.this);
    }


    public boolean passPayload(String payload) {

        String passingPayload = payload;

        String passingTopic = "healthxtream/send/";
        isPublished=false;

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        if (isPublished)
            Toast.makeText(DoctorWaitingAreaActivity.this,"Message Published", Toast.LENGTH_SHORT).show();

        Log.d("TagpassedPayload", passingPayload);

        return isPublished;
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");

        if(reason.equals("isDocAvailable")) {
            //sample msg = {"reason":"docIsAvailable", "did":"doctor1"}
            isDocAvailable(jsonResponse);
        }

        Toast.makeText(DoctorWaitingAreaActivity.this,"Message arrived: "+jsonResponse, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    private void isDocAvailable(String res){
        String pid=JsonAccess.getJsonInsideObj(res,"pid");
        String payloadToBeSend="{\"reason\":\"docIsAvailable\", \"pid\":\""+pid+"\", \"did\":\""+doctorID+"\"}";
        //sample: {"reason":"isDocAvailable", "pid":"fa18a0ec-974d-4d26-ba26-bcb67a84c0ee"}
       passPayload(payloadToBeSend);
    }







}
