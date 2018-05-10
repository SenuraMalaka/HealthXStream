package com.example.senura.healthxstream;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.example.senura.healthxstream.mqttConnectionPackage.uniqueIDgenerator;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
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
    String clientID= uniqueIDgenerator.getUUID();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        //email added delgahadeniya.dissanayake@students.plymouth.ac.uk

       setButtons();
       connectMqttClient();


    }






    public void passPayload(String payload) {

        String passingPayload =payload;

        String passingTopic = "healthxtream/send/";
        isPublished=false;

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        Log.d("TagpassedPayload", passingPayload);

    }







    //Mqtt overrides -start

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void connectMqttClient() {
        client = mConnection.connect(MainActivity.this, this, "healthxtream/patient/"+clientID, false);
        client.setCallback(MainActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.w("TAG", "connection lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {


    }






    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    //Mqtt -end







    private void setButtons(){
        ImageButton button_HB;
        ImageButton button_DoctorLogin;
        ImageButton button_MD;
        ImageButton button_HX_Logo;
        ImageButton button_BT;



        //BodyTemp
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_BT);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BodyTemperatureActivity.class));
            }
        });


        //Button_HXLogo
        button_HX_Logo = (ImageButton) findViewById(R.id.imageButton_MM_Logo);

        button_HX_Logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"HealthXStream V1.0", Toast.LENGTH_SHORT).show();
            }
        });


        //MyDocs
        button_MD = (ImageButton) findViewById(R.id.imageButton_MM_MD);

        button_MD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goToDoctorsAct();

            }
        });




        //DocLogin
        button_DoctorLogin = (ImageButton) findViewById(R.id.imageButton_MM_FM);

        button_DoctorLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectClient();
                goToLoginAct();
                finish();
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


    @Override
    public void onStop() {
        super.onStop();
    }


    private void disconnectClient()
    {
        if(client!=null) {
            try {
                IMqttToken disconToken = client.disconnect();
                disconToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // we are now successfully disconnected
                        Log.d("DocIll", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocIll", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocIll", "Client Disconnect -error " + e.toString());
            }
        }
    }




    private void goToDoctorsAct(){
        //go to another act
        Intent myIntent = new Intent(MainActivity.this, MyDoctorsActivity.class);
        myIntent.putExtra("clientID", clientID); //Optional parameters
        MyDoctorsActivity.clientTemp=client;//setMqttclient
        MyDoctorsActivity.mConnectionTemp=mConnection;//setMqttConnection
        MainActivity.this.startActivity(myIntent);
        finish();
    }


    private void goToLoginAct(){
        //go to another act
        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(myIntent);

    }





}
