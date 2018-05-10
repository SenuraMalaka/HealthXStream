package com.example.senura.healthxstream;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.DoctorsAct.DoctorWaitingAreaActivity;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MyDoctorsActivity extends AppCompatActivity implements MqttCallback {

    //mqtt Variables
    public static MqttConnection mConnectionTemp=null;
    private MqttConnection mConnection=null;
    public static MqttAndroidClient clientTemp = null;
    private MqttAndroidClient client = null;

    private String clientID=null;

    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;

    String docLst=null;


    //Handler
    private int mInterval = 6000;
    private Handler mHandler;


    //listView vars
    private ListView lv=null;
    private List<String> doctors_list=null;
    private  ArrayAdapter<String> arrayAdapter=null;

    ArrayList<String> availableDidList=null;
    JSONArray docListjArray = null;

    private boolean isRetainMqttState=false;

    private String patientName="Sen Ma";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);



        Intent intent = getIntent();
        clientID = intent.getStringExtra("clientID");

        mConnection=mConnectionTemp;
        client=clientTemp;
        setClientListenToThisAct();



        // Get reference of widgets from XML layout
        lv = (ListView) findViewById(R.id.ListView_MyDocs);

        // Initializing a new String Array
        String[] doctorsArray = new String[]{};

        // Create a List from String Array elements
        doctors_list = new ArrayList<String>(Arrays.asList(doctorsArray));

        // Create an ArrayAdapter from List
        arrayAdapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, doctors_list);

        // DataBind ListView with items from ArrayAdapter
        lv.setAdapter(arrayAdapter);




        //connectMqttClient();
        setButtons();


    }

    private void setClientListenToThisAct(){
        client.setCallback(MyDoctorsActivity.this);
    }





    private void setButtons(){
        Button button_sentText;
        Button button_GoBack;

        //mqtt
        button_sentText = (Button) findViewById(R.id.button_sendText);

        button_GoBack = (Button) findViewById(R.id.button_MD_GoBack);

        button_sentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                availableDidList= new ArrayList<String>();//should only run once
                arrayAdapter.clear();//flush the view

                passDocListReq();

                //a loop with did should go here //pName should assign somehow


                passPayload("{\"reason\":\"isDocAvailable\", \"pid\":\""+clientID+"\", \"did\":\"345\", \"pName\":\"Sen Ma\"}");


                //hide the text
                TextView textView_checkToSeeDocs = (TextView) findViewById(R.id.textView_checkToSeeTheDocs);
                textView_checkToSeeDocs.setVisibility(View.GONE);


            }
        });



        button_GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //back to main menu
                finish();

            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String didJson="";
                try {
                    didJson=docListjArray.getString(position);
                    didJson="["+didJson+"]";
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray didJsonArray=null;
                try {
                    didJsonArray = new JSONArray(didJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                JSONObject json_array = didJsonArray.optJSONObject(0);

                Iterator<?> keys = json_array.keys();

                String did="";
                String docName="";
                while( keys.hasNext() ) {
                    did=(String) keys.next();
                    try {
                        docName= (String) json_array.get(did);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }



                if(doctors_list.get(position).toLowerCase().contains("available"))
                showBookDoctorAlert(docName,did);
                else Toast.makeText(MyDoctorsActivity.this,"Selected Doctor is not available right now", Toast.LENGTH_SHORT).show();

            }
        });
    }


    public boolean passPayload(String payload) {

        String android_serial = android.os.Build.SERIAL;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateAndTime = df.format(Calendar.getInstance().getTime());


        String passingPayload = payload;

        String passingTopic = "healthxtream/send/";//+client.getClientId();
        isPublished=false;

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        Log.d("TagpassedPayload", passingPayload);

        return isPublished;
    }



    //get the doc list
    public void passDocListReq() {
        passPayload("{\"reason\":\"docListReq\", \"pid\":\""+clientID+"\"}");
    }



    private void connectMqttClient() {
        client = mConnection.connect(MyDoctorsActivity.this, this, "healthxtream/patient/"+clientID, false);
        client.setCallback(MyDoctorsActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(MyDoctorsActivity.this,"Connection Lost due to unreliable network", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        //Log.d("TagMessageArrived", jsonResponse);

        Log.d("TagMessageArrived", jsonResponse);

        String reason=JsonAccess.getJsonInsideObj(jsonResponse,"reason");




        if(reason.equals("docIsAvailable")) {
            //sample msg = {"reason":"docIsAvailable", "did":"doctor1"}
            docIsAvailable(jsonResponse);
        }else if(reason.equals("docListReq")){
            //sample event = {"reason":"docListReq", "docList":[{"345":"Doctor1 Name"},{"434":"Doctor2 Name"},{"543":"Doctor3 Name"}]}
            docListReq(jsonResponse);
        }

        //Toast.makeText(MyDoctorsActivity.this, "response : "+jsonResponse, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
            Toast.makeText(MyDoctorsActivity.this,"Message Published", Toast.LENGTH_SHORT).show();
    }


    private void docIsAvailable(String jsonRes){



        String did =JsonAccess.getJsonInsideObj(jsonRes,"did");

        int resCount=0;
        JSONArray jArray = null;
        try {
            jArray = new JSONArray(docLst);
            resCount=jArray.length();
            doctors_list.clear();
        }catch (Exception ex){}



        for(int i=0;i<resCount;i++) {

                JSONObject json_array = jArray.optJSONObject(i);

                Iterator<?> keys = json_array.keys();

                while (keys.hasNext()) {
                    String val = "";
                    String key = (String) keys.next();
                    System.out.println("Key: " + key);
                    try {
                        val = (String) json_array.get(key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    if(isDidArrayHas(key)){
                        val+=" AVAILABLE";
                    }
                    else if(key.equals(did)) {
                        val+=" AVAILABLE";
                    availableDidList.add(key);
                    }

                    doctors_list.add(key + " -- " + val);

                }

        }

        //doctors_list.add(JsonAccess.getJsonInsideObj(jsonRes,"did"));

        arrayAdapter.notifyDataSetChanged();
    }


    private boolean isDidArrayHas(String key){

        boolean state=false;
        for(int i=0;i<availableDidList.size();i++){

            if(key.equals(availableDidList.get(i))){
                state=true;
            }
        }

         return state;
    }

    private void docListReq(String jsonRes){

        arrayAdapter.clear();//flush the view

         //doctors_list.add(JsonAccess.getJsonInsideObj(jsonRes,"docList"));
         String jsonArrayDocResult=JsonAccess.getJsonInsideObj(jsonRes,"docList");

        docLst=JsonAccess.getJsonInsideObj(jsonRes,"docList");
        //[{"345":"Doctor1 Name"},{"434":"Doctor2 Name"},{"543":"Doctor3 Name"}]


        int resCount=0;

        try {
            docListjArray = new JSONArray(docLst);
            resCount=docListjArray.length();
        }catch (Exception ex){}





        for(int i=0;i<resCount;i++) {
            {
                JSONObject json_array = docListjArray.optJSONObject(i);


                Iterator<?> keys = json_array.keys();

                while (keys.hasNext()) {
                    String val = "";
                    String key = (String) keys.next();
                    System.out.println("Key: " + key);
                    try {
                        val = (String) json_array.get(key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    doctors_list.add(key + " -- " + val);

                }
            }
        }

        //Toast.makeText(MyDoctorsActivity.this, "array index 2 is : "+res, Toast.LENGTH_LONG).show();


        //Continue from here -Sen
        arrayAdapter.notifyDataSetChanged();
    }


    private void showBookDoctorAlert(final String docName, final String did){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MyDoctorsActivity.this);

        // set title
        alertDialogBuilder.setTitle("Book a Doctor");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to book Dr. "+docName+"?")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //isDisplayingAnAlert=false;
                        Toast.makeText(MyDoctorsActivity.this, "Sending the booking request", Toast.LENGTH_SHORT).show();
                        passBookDocNow(did);

                        goToDoctorContactAct(docName,did);
                    }
                })
                .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //isDisplayingAnAlert=false;
                        Toast.makeText(MyDoctorsActivity.this, "Canceled the booking", Toast.LENGTH_SHORT).show();

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        //isDisplayingAnAlert=true;
        alertDialog.show();
    }


    private void passBookDocNow(String did) {
        passPayload("{\"reason\":\"bookDocNow\", \"pid\":\""+clientID+"\", \"did\":\""+did+"\", \"pName\":\""+patientName+"\"}");
    }

    private void goToDoctorContactAct(String docName, String did){
        //go to another act
        Intent myIntent = new Intent(MyDoctorsActivity.this, DoctorContactActivity.class);
        myIntent.putExtra("did", did); //Optional parameters
        myIntent.putExtra("docName", docName); //Optional parameters
        myIntent.putExtra("clientID", clientID); //Optional parameters
        DoctorContactActivity.clientTemp=client;//setMqttclient
        DoctorContactActivity.mConnectionTemp=mConnection;//setMqttConnection
        MyDoctorsActivity.this.startActivity(myIntent);
        isRetainMqttState=true;
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
                        Log.d("MyDocA", "Client Successfully Disconnected");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // something went wrong, but probably we are disconnected anyway
                        Log.w("MyDocA", "Client is not properly disconnected");
                    }
                });
            } catch (MqttException e) {
                Log.e("MyDocA", "Client Disconnect -error " + e.toString());
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isRetainMqttState) {
            disconnectClient();
        }
    }







}
