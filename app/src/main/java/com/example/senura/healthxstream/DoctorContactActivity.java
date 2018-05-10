package com.example.senura.healthxstream;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class DoctorContactActivity extends AppCompatActivity implements MqttCallback{

    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    private String clientID=null;
    private String did=null;
    private String docName=null;

    public String jsonResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_contact);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);



        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");

        mConnection=mConnectionTemp;
        client=clientTemp;
        setClientListenToThisAct();
        setRes();
        Toast.makeText(DoctorContactActivity.this,"clientID -> "+clientID, Toast.LENGTH_SHORT).show();

    }


    private void setClientListenToThisAct(){
        client.setCallback(DoctorContactActivity.this);
    }


    private void setRes(){
        TextView textView_docID;
        TextView textView_docNAme;
        Button button_CancelAppointment;

        textView_docID = (TextView) findViewById(R.id.textView_DC_waitingDocID);
        textView_docNAme = (TextView) findViewById(R.id.textView_DC_waitingDocName);
        button_CancelAppointment = (Button) findViewById(R.id.button_DC_CancelAp);

        textView_docID.setText("Doctor ID - "+did);
        textView_docNAme.setText("Doctor Name - "+docName);

        button_CancelAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDisconnectMessage();
                finish();
            }
        });

    }



    public void passDisconnectMessage() {

        if(did!=null) {
            String passingPayload = "{\"reason\":\"pStopped\",\"pid\":\"" + clientID + "\"}";

            String passingTopic = "healthxtream/doctor/"+did;

            mConnection.publishMessage(passingPayload, passingTopic);
        }

    }


    @Override
    public void connectionLost(Throwable cause) {
        Intent myIntent = new Intent(DoctorContactActivity.this, MainActivity.class);
        DoctorContactActivity.this.startActivity(myIntent);
        finish();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");


        Toast.makeText(DoctorContactActivity.this,"arrived -> "+message.toString(), Toast.LENGTH_SHORT).show();

        if (reason.equals("bookDocConfirmStatus")) {
            //sample msg = {"reason":"bookDocConfirmStatus", "pid":"patient123", "did":"doctor123", "state":true}
            docIsAvailable_resHandler(jsonResponse);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }



    private void docIsAvailable_resHandler(String res){
        boolean state= Boolean.valueOf(JsonAccess.getJsonInsideObj(res,"state"));
        String _did=JsonAccess.getJsonInsideObj(res,"did");

        if(_did.equals(did)){

            if(state){
                //confirmed
                Toast.makeText(DoctorContactActivity.this,"Doctor Requests Your Illness Info...", Toast.LENGTH_SHORT).show();
                goToIllnessAwarenessAct(docName, did);//go to Illness Awareness act

            }else{
                //Doctor cancelled the booking
                disconnectClient();
                Toast.makeText(DoctorContactActivity.this,"Doctor Rejected the Appointment", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DoctorContactActivity.this, MainActivity.class));
                finish();
            }
        }else{
            disconnectClient();
            Toast.makeText(DoctorContactActivity.this,"Doctor cannot be authorised..!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DoctorContactActivity.this, MainActivity.class));
            finish();
        }
    }


    private void goToIllnessAwarenessAct(String docName, String did){
        //go to another act
        Intent myIntent = new Intent(DoctorContactActivity.this, DoctorIllnessAwarenessActivity.class);
        myIntent.putExtra("did", did); //Optional parameters
        myIntent.putExtra("docName", docName); //Optional parameters
        myIntent.putExtra("clientID", clientID); //Optional parameters
        DoctorIllnessAwarenessActivity.clientTemp=client;//setMqttclient
        DoctorIllnessAwarenessActivity.mConnectionTemp=mConnection;//setMqttConnection
        DoctorContactActivity.this.startActivity(myIntent);
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
                        Log.d("DocCA", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocCA", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocCA", "Client Disconnect -error " + e.toString());
            }
        }
    }

}
