package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.DoctorsAct.DoctorWaitingAreaActivity;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DoctorIllnessAwarenessActivity extends AppCompatActivity implements MqttCallback {

    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;


    private String clientID=null;
    private String did=null;
    private String docName=null;

    private String jsonResponse = null;

    private EditText editText_illnessInfo=null;

    private String patientName="Sen Ma";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_illness_awareness);

        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");

        mConnection=mConnectionTemp;
        client=clientTemp;
        setResources();
        setClientListenToThisAct();
    }

    private void setClientListenToThisAct(){
        client.setCallback(DoctorIllnessAwarenessActivity.this);
    }


    private void setResources(){
        editText_illnessInfo = (EditText) findViewById(R.id.editText_DIA_IllnessInfo);


        Button button_sendInfo;

        button_sendInfo = (Button) findViewById(R.id.button_DIA_SendIllness);

        button_sendInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmyInfo(editText_illnessInfo.getText().toString());
            }
        });

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


    public boolean passPayload(String payload) {

        String android_serial = android.os.Build.SERIAL;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateAndTime = df.format(Calendar.getInstance().getTime());


        String passingPayload = payload;

        String passingTopic = "healthxtream/send/";//+client.getClientId();

        boolean isPublished =mConnection.publishMessage(passingPayload, passingTopic);

        if (isPublished)
            Toast.makeText(DoctorIllnessAwarenessActivity.this,"Message Published", Toast.LENGTH_SHORT).show();

        Log.d("TagpassedPayload", passingPayload);

        return isPublished;
    }



    private void sendmyInfo(String text){
        //send this - {"reason":"myInfo", "pid":"patient123", "did":"doctor123", "name":"senura", "msg":"I am having a fever"}
        Boolean state =passPayload("{\"reason\":\"myInfo\", \"pid\":\""+clientID+"\", \"did\":\""+did+"\", \"name\":\""+patientName+"\", \"msg\":\""+text+"\"}");
        if(state){
            Toast.makeText(DoctorIllnessAwarenessActivity.this,"Doctor will join...", Toast.LENGTH_SHORT).show();
            goToDoctorDiagnoseAct(docName, did);
        }else{
            disconnectClient();
            startActivity(new Intent(DoctorIllnessAwarenessActivity.this, MainActivity.class));
            finish();
        }
    }




    //after publishing the myInfo
    private void goToDoctorDiagnoseAct(String docName, String did){
        //go to another act
        Intent myIntent = new Intent(DoctorIllnessAwarenessActivity.this, DoctorDiagnoseActivity.class);
        myIntent.putExtra("did", did); //Optional parameters
        myIntent.putExtra("docName", docName); //Optional parameters
        myIntent.putExtra("clientID", clientID); //Optional parameters
        DoctorDiagnoseActivity.clientTemp=client;//setMqttclient
        DoctorDiagnoseActivity.mConnectionTemp=mConnection;//setMqttConnection
        DoctorIllnessAwarenessActivity.this.startActivity(myIntent);
        finish();
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


}
