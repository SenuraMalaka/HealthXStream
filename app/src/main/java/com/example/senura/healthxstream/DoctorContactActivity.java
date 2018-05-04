package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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



        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");

        mConnection=mConnectionTemp;
        client=clientTemp;
        setClientListenToThisAct();
    }


    private void setClientListenToThisAct(){
        client.setCallback(DoctorContactActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");


        //Toast.makeText(DoctorContactActivity.this,"arrived -> "+message.toString(), Toast.LENGTH_SHORT).show();

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
                Toast.makeText(DoctorContactActivity.this,"Doctor will join...", Toast.LENGTH_SHORT).show();
                goToDoctorDiagnoseAct(docName, did);//go to diagnose act

            }else{
                //Doctor cancelled the booking
                disconnectClient();
                Toast.makeText(DoctorContactActivity.this,"Doctor Rejected the Appointment", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DoctorContactActivity.this, MainActivity.class));
                finish();
            }
        }
    }



    private void goToDoctorDiagnoseAct(String docName, String did){
        //go to another act
        Intent myIntent = new Intent(DoctorContactActivity.this, DoctorDiagnoseActivity.class);
        myIntent.putExtra("did", did); //Optional parameters
        myIntent.putExtra("docName", docName); //Optional parameters
        myIntent.putExtra("clientID", clientID); //Optional parameters
        DoctorDiagnoseActivity.clientTemp=client;//setMqttclient
        DoctorDiagnoseActivity.mConnectionTemp=mConnection;//setMqttConnection
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
