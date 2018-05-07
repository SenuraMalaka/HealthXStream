package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DoctorDiagnoseActivity extends AppCompatActivity implements MqttCallback {

    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    private String clientID=null;
    private String did=null;
    private String docName=null;

    private String jsonResponse = null;
    private boolean isFirstTimeMessageBoxUpdates=false;

    //View
    TextView textView_MessageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_diagnose);

        Intent intent = getIntent();
        did = intent.getStringExtra("did");
        docName = intent.getStringExtra("docName");
        clientID = intent.getStringExtra("clientID");
        Toast.makeText(DoctorDiagnoseActivity.this,"did is ="+did+" \ndocName ="+docName, Toast.LENGTH_SHORT).show();

        mConnection=mConnectionTemp;
        client=clientTemp;
        setResources();

        setClientListenToThisAct();
    }


    private void setClientListenToThisAct(){
        client.setCallback(DoctorDiagnoseActivity.this);
    }



    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");


        Toast.makeText(DoctorDiagnoseActivity.this,"arrived -> "+message.toString(), Toast.LENGTH_SHORT).show();

        if (reason.equals("docMsg")) {
            //sample msg = {"reason":"docMsg", "pid":"patient123", "did":"doctor123", "sensorType":"temp", "msg":"Please scan the temp"}
           docMsg_resHandler(jsonResponse);
        }else if (reason.equals("docStopped")) {
            //sample msg = {"reason":"docStopped","did":"doc1234"}
            String _did=JsonAccess.getJsonInsideObj(jsonResponse,"did");

            if(_did.equals(did)){
                goToMainMenu("Doctor Disconnected..!");
            }

        }


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }



    //when Other one Stopped
    private void goToMainMenu(String msg){
        //go to another act
        Toast.makeText(DoctorDiagnoseActivity.this , msg, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(DoctorDiagnoseActivity.this, MainActivity.class);
        DoctorDiagnoseActivity.this.startActivity(myIntent);
        finish();
    }


    private void docMsg_resHandler(String res){
        //{"reason":"docMsg", "pid":"patient123", "did":"doctor123", "sensorType":"temp", "msg":"Please scan the temp"}

        String _did=JsonAccess.getJsonInsideObj(res,"did");
        String _sensorType=JsonAccess.getJsonInsideObj(res,"sensorType");
        String _msg=JsonAccess.getJsonInsideObj(res,"msg");

        Toast.makeText(DoctorDiagnoseActivity.this,"did is- "+did+" _did -"+_did, Toast.LENGTH_SHORT).show();


        if(did.equals(_did)){

            if(_sensorType.equals("null")){
                //no sensor involved

                Toast.makeText(DoctorDiagnoseActivity.this,"Inside 1st if", Toast.LENGTH_SHORT).show();

                if(!isFirstTimeMessageBoxUpdates){
                    isFirstTimeMessageBoxUpdates=true;
                    textView_MessageBox.setText("");//only runs once
                    Toast.makeText(DoctorDiagnoseActivity.this,"Inside 2nd if", Toast.LENGTH_SHORT).show();

                }
                addTextToMsgBox("Doctor -> "+_msg);

            }


            if(_sensorType.equals("temp")){

            }else if(_sensorType.equals("pulse")){

            }else{
                //payload format is wrong //could be null
            }

        }

    }


    private void setResources(){
        textView_MessageBox = (TextView) findViewById(R.id.textView_DD_Messages);
    }


    private void addTextToMsgBox(String text){
        textView_MessageBox.setText(textView_MessageBox.getText().toString()+"\n-----\n"+text);
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
                        Log.d("DocD", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocD", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocD", "Client Disconnect -error " + e.toString());
            }
        }
    }


}
