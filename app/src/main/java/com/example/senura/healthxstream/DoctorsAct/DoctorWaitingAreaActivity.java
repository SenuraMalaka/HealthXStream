package com.example.senura.healthxstream.DoctorsAct;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.senura.healthxstream.BodyTemperatureActivity;
import com.example.senura.healthxstream.MainActivity;
import com.example.senura.healthxstream.MyDoctorsActivity;
import com.example.senura.healthxstream.R;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.example.senura.healthxstream.mqttConnectionPackage.uniqueIDgenerator;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DoctorWaitingAreaActivity extends AppCompatActivity implements MqttCallback {

    //MQTT Vars
    private MqttConnection mConnection = new MqttConnection();
    private MqttAndroidClient client = null;
    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;
    String doctorID=null;

    //ListView Vars
    private ListView lv=null;
    private List<String> patients_list=null;
    private ArrayAdapter<String> arrayAdapter=null;

    //Alert
    private boolean isDisplayingAnAlert=false;

    //Buttons
    Button button_Search=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_waiting_area);

        doctorID="345";//should get the docID from the login
        setButtons();

        // Get reference of widgets from XML layout
        lv = (ListView) findViewById(R.id.ListView_MyPatients);

        // Initializing a new String Array
        String[] doctorsArray = new String[]{};

        // Create a List from String Array elements
        patients_list = new ArrayList<String>(Arrays.asList(doctorsArray));

        // Create an ArrayAdapter from List
        arrayAdapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, patients_list);

        // DataBind ListView with items from ArrayAdapter
        lv.setAdapter(arrayAdapter);


        //showAlert("sen");


    }


    private void hideSearchTextArea(boolean state){
        LinearLayout lL_Search;
        lL_Search = (LinearLayout) findViewById(R.id.linearLayout_DWA_SearchingTextArea);

        if(state)
        lL_Search.setVisibility(View.GONE);
        else lL_Search.setVisibility(View.VISIBLE);
    }

    private void setButtons(){

        //BodyTemp
        button_Search = (Button) findViewById(R.id.button_DWA_Search);
        button_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectMqttClient();//subscribe to topic
                hideSearchTextArea(false);
                button_Search.setVisibility(View.GONE);
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

        if(!isDisplayingAnAlert) {
            if (reason.equals("isDocAvailable")) {
                //sample msg = {"reason":"docIsAvailable", "did":"doctor1"}
                isDocAvailable_res(jsonResponse);
            } else if (reason.equals("bookDocNow")) {
                //sample msg = {"reason":"bookDocNow", "pid":"patient123", "pName":"patient1"}
                bookDocNow_res(jsonResponse);
            }
        }//if (isDisplayingAnAlert) - end

        Toast.makeText(DoctorWaitingAreaActivity.this,"Message arrived: "+jsonResponse, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    private void isDocAvailable_res(String res){

        String pName=JsonAccess.getJsonInsideObj(res,"pName");
        String pid=JsonAccess.getJsonInsideObj(res,"pid");

        if(!isPatientArrayHas(pid +" - "+pName))
        addToPatientList(pid +" - "+pName);


        String payloadToBeSend="{\"reason\":\"docIsAvailable\", \"pid\":\""+pid+"\", \"did\":\""+doctorID+"\"}";
        //sample: {"reason":"isDocAvailable", "pid":"fa18a0ec-974d-4d26-ba26-bcb67a84c0ee"}
       passPayload(payloadToBeSend);
    }


    private boolean isPatientArrayHas(String pidANDpName){

        Toast.makeText(DoctorWaitingAreaActivity.this,"pArray : : "+patients_list, Toast.LENGTH_LONG).show();


        boolean state=false;
        for(int i=0;i<patients_list.size();i++){

            if(pidANDpName.equals(patients_list.get(i))){
                state=true;
            }
        }

        return state;
    }


    private void addToPatientList(String pidANDpName){
        patients_list.add(pidANDpName);
        arrayAdapter.add(pidANDpName);
    }


    private void bookDocNow_res(String res){
        //{"reason":"bookDocNow", "pid":"patient123", "did":"doctor123", "pName":"patient1"}

        String pName=JsonAccess.getJsonInsideObj(res,"pName");
        String pid=JsonAccess.getJsonInsideObj(res,"pid");
        showAppointmentAlert(pName,pid);
    }





    private void showAppointmentAlert(String pName, final String pid){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DoctorWaitingAreaActivity.this);

        // set title
        alertDialogBuilder.setTitle("Appointment");

        // set dialog message
        alertDialogBuilder
                .setMessage("Patient named \""+pName+"\" has made an appointment with you right now.\n Would you like to accept?")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        isDisplayingAnAlert=false;
                        sendTheConfirmationStatus(pid, true);
                        Toast.makeText(DoctorWaitingAreaActivity.this, "OK button click", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isDisplayingAnAlert=false;
                        sendTheConfirmationStatus(pid, false);

                        dialog.cancel();
                        //reload the page
                        startActivity(new Intent(DoctorWaitingAreaActivity.this, DoctorWaitingAreaActivity.class));
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        isDisplayingAnAlert=true;
        alertDialog.show();
    }


    private void sendTheConfirmationStatus(String pid,boolean state){
        passPayload("{\"reason\":\"bookDocConfirmStatus\", \"pid\":\""+pid+"\", \"did\":\""+doctorID+"\", \"state\":"+state+"}");
    }



    @Override
    public void onResume(){
        super.onResume();
        arrayAdapter.clear();
        patients_list=new ArrayList<String>();
        button_Search.setVisibility(View.VISIBLE);
        hideSearchTextArea(true);
    }


    @Override
    public void onPause() {
        super.onPause();
        disconnectClient();
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
                        Log.d("DocWA", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("DocWA", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("DocWA", "Client Disconnect -error " + e.toString());
            }
        }
    }







}
