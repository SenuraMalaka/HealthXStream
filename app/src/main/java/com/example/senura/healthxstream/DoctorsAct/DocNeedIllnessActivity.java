package com.example.senura.healthxstream.DoctorsAct;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.LoginActivity;
import com.example.senura.healthxstream.R;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DocNeedIllnessActivity extends AppCompatActivity implements MqttCallback {

    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    //MQTT Vars
    private boolean isPublished = false;
    public String jsonResponse = null;



    //res
    Button button_Agree=null;
    Button button_Cancel=null;
    TextView textView_Msg=null;
    LinearLayout linearLayout_BtnAgree=null;


    private boolean isRetainMqttState=false;


    private String patientID=null;
    private String doctorID=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_need_illness);


        Intent intent = getIntent();
        doctorID = intent.getStringExtra("did");
        patientID = intent.getStringExtra("pid");

        mConnection=mConnectionTemp;
        client=clientTemp;

        initRes();
        setClientListenToThisAct();
    }


    private void setClientListenToThisAct(){
        client.setCallback(DocNeedIllnessActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");

            if (reason.equals("myInfo")) {
                //sample msg = {"reason":"myInfo", "pid":"patient123", "did":"doctor123", "name":"senura", "msg":"I am having a fever"}
                updateTheTextViewMessage(jsonResponse);

            } else if (reason.equals("pStopped")) {
                //sample msg = {"reason":"pStopped","pid":"pid12345"}
                String _pid=JsonAccess.getJsonInsideObj(jsonResponse,"pid");

                if(_pid.equals(patientID)){
                    goToLogin("Patient Disconnected..!");
                }

            }////

    }///////////msgAr - - - End



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

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
                        Log.d("DocNILL", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocNILL", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocNILL", "Client Disconnect -error " + e.toString());
            }
        }
    }


    //when Other one Stopped
    private void goToLogin(String msg){
        //go to another act
        Toast.makeText(DocNeedIllnessActivity.this , msg, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(DocNeedIllnessActivity.this, LoginActivity.class);
        DocNeedIllnessActivity.this.startActivity(myIntent);
        finish();
    }



    private void goToPatientDiagnoseAct(String msg){
        //go to another act
        Toast.makeText(DocNeedIllnessActivity.this , msg, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(DocNeedIllnessActivity.this, PatientDiagnoseActivity.class);
        myIntent.putExtra("did", doctorID); //Optional parameters
        myIntent.putExtra("pid", patientID); //Optional parameters
        PatientDiagnoseActivity.clientTemp=client;//setMqttclient
        PatientDiagnoseActivity.mConnectionTemp=mConnection;//setMqttConnection
        DocNeedIllnessActivity.this.startActivity(myIntent);
        finish();
    }


    public void passDisconnectMessage() {

        if(patientID!=null) {
            String passingPayload = "{\"reason\":\"docStopped\",\"did\":\"" + doctorID + "\"}";

            String passingTopic = "healthxtream/patient/"+patientID;

            mConnection.publishMessage(passingPayload, passingTopic);
        }

    }


    private void updateTheTextViewMessage(String res) {
        //sample msg = {"reason":"myInfo", "pid":"patient123", "did":"doctor123", "name":"senura", "msg":"I am having a fever"}
        String _pName=JsonAccess.getJsonInsideObj(jsonResponse,"name");
        String _Msg=JsonAccess.getJsonInsideObj(jsonResponse,"msg");

        textView_Msg.setText(_pName+" says his illness is "+_Msg);
        linearLayout_BtnAgree.setVisibility(View.VISIBLE);
    }

    private void initRes(){
        textView_Msg= (TextView) findViewById(R.id.textView_DNI_waiting);
        button_Agree= (Button) findViewById(R.id.button_DNI_Agree);
        button_Cancel= (Button) findViewById(R.id.button_DNI_CancelAp);
        linearLayout_BtnAgree= (LinearLayout) findViewById(R.id.linearLayout_DNI_AgreeButton);
        linearLayout_BtnAgree.setVisibility(View.INVISIBLE);


        button_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel
                passDisconnectMessage();
                goToLogin("Successfully canceled the Appointment..!");
            }
        });


        button_Agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAgreePayload();
                isRetainMqttState=true;
                goToPatientDiagnoseAct("Patient is Ready to chat");
            }
        });

    }

    private void sendAgreePayload(){
        if(patientID!=null) {
            String passingPayload = "{\"reason\":\"illnessAgreed\",\"did\":\"" + doctorID + "\"}";

            String passingTopic = "healthxtream/patient/"+patientID;

            mConnection.publishMessage(passingPayload, passingTopic);
        }
    }



    @Override
    public void onStop() {
        super.onStop();

        if(!isRetainMqttState) {
            passDisconnectMessage();
            disconnectClient();
        }
    }



}
