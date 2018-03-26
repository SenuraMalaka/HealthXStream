package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements MqttCallback {



    private MqttConnection mConnection = new MqttConnection();
    private MqttAndroidClient client = null;
    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //email added delgahadeniya.dissanayake@students.plymouth.ac.uk

       setButtons();


    }






    public void passPayload() {

        String android_serial = android.os.Build.SERIAL;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateAndTime = df.format(Calendar.getInstance().getTime());

        String passingPayload = "sample test25";

        String passingTopic = "terminal/test25/"+client.getClientId();

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        if (isPublished)
            Toast.makeText(MainActivity.this,"Message Published", Toast.LENGTH_SHORT).show();

        Log.d("TagpassedPayload", passingPayload);

    }







    //Mqtt overrides -start

    @Override
    protected void onResume() {
        super.onResume();
        connectMqttClient();
    }

    private void connectMqttClient() {
        SourceID="test25";
        client = mConnection.connect(MainActivity.this, this, "terminal/" + SourceID + "/", true);
        client.setCallback(MainActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.w("TAG", "connection lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        this.jsonResponse = message.toString();

        //Log.d("TagMessageArrived", jsonResponse);


        Log.d("TagMessageArrived", jsonResponse);

        Toast.makeText(MainActivity.this, "response : "+jsonResponse, Toast.LENGTH_LONG).show();



    }






    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    //Mqtt -end







    private void setButtons(){
        ImageButton button_HB;
        ImageButton button_FM;
        ImageButton button_MD;
        ImageButton button_BT;



        //BodyTemp
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_BT);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BodyTemperatureActivity.class));
            }
        });


        //MyDocs
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_MD);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(MainActivity.this, MyDoctorsActivity.class);
                myIntent.putExtra("key", "Achala Dissanayake"); //Optional parameters
                MainActivity.this.startActivity(myIntent);

            }
        });




        //FamilyMems
        button_FM = (ImageButton) findViewById(R.id.imageButton_MM_FM);

        button_FM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, FamilyMembersActivity.class));
                passPayload();

            }
        });


        //hearbeat
        button_HB = (ImageButton) findViewById(R.id.imageButton_MM_HB);

        button_HB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HeartBeatActivity.class));

            }
        });
    }


}
