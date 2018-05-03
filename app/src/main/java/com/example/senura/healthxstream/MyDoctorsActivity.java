package com.example.senura.healthxstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.example.senura.healthxstream.mqttConnectionPackage.uniqueIDgenerator;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
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


    private MqttConnection mConnection = new MqttConnection();
    private MqttAndroidClient client = null;
    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;
    String clientID=uniqueIDgenerator.getUUID();


    private ListView lv=null;
    private List<String> doctors_list=null;
    private  ArrayAdapter<String> arrayAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);

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

        Intent intent = getIntent();
        String value = intent.getStringExtra("key");

        //fruits_list.add(value);

        //arrayAdapter.notifyDataSetChanged();


        docListReq("{\"reason\":\"docListReq\", \"docList\":[{\"345\":\"Doctor1 Name\"},{\"434\":\"Doctor2 Name\"},{\"543\":\"Doctor3 Name\"}]}");
        //connectMqttClient();
        //setButtons();


    }





    private void setButtons(){
        Button button_sentText;



        //mqtt
        button_sentText = (Button) findViewById(R.id.button_sendText);

        button_sentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MyDoctorsActivity.this, BodyTemperatureActivity.class));
                passPayload();
                arrayAdapter.clear();
                //arrayAdapter.notifyDataSetChanged();
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text or do whatever you need.
                Toast.makeText(MyDoctorsActivity.this,"postion: "+position+" Long: "+id+" value is "+arrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
        });


    }


    public void passPayload() {

        String android_serial = android.os.Build.SERIAL;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateAndTime = df.format(Calendar.getInstance().getTime());


        String passingPayload = "{\"reason\":\"isDocAvailable\", \"pid\":\""+clientID+"\", \"did\":\"doctor1\"}";

        String passingTopic = "healthxtream/send/";//+client.getClientId();
        isPublished=false;

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        if (isPublished)
            Toast.makeText(MyDoctorsActivity.this,"Message Published", Toast.LENGTH_SHORT).show();

        Log.d("TagpassedPayload", passingPayload);

    }



    //get the doc list
    public void passDocListReq() {

       String passingPayload = "{\"reason\":\"docListReq\", \"pid\":\""+clientID+"\"}";

        String passingTopic = "healthxtream/send/";//+client.getClientId();
        isPublished=false;

        isPublished = mConnection.publishMessage(passingPayload, passingTopic);

        if (isPublished)
            Toast.makeText(MyDoctorsActivity.this,"Message Published", Toast.LENGTH_SHORT).show();

        Log.d("TagpassedPayload", passingPayload);

    }



    private void connectMqttClient() {
        client = mConnection.connect(MyDoctorsActivity.this, this, "healthxtream/patient/"+clientID, false);
        client.setCallback(MyDoctorsActivity.this);
    }


    @Override
    public void connectionLost(Throwable cause) {
        //connection has been lost
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

        Toast.makeText(MyDoctorsActivity.this, "response : "+jsonResponse, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    private void docIsAvailable(String jsonRes){
         doctors_list.add(JsonAccess.getJsonInsideObj(jsonRes,"did"));
        arrayAdapter.notifyDataSetChanged();
    }

    private void docListReq(String jsonRes){
         //doctors_list.add(JsonAccess.getJsonInsideObj(jsonRes,"docList"));
         String jsonArrayDocResult=JsonAccess.getJsonInsideObj(jsonRes,"docList");

        String docLst=JsonAccess.getJsonInsideObj(jsonRes,"docList");
        //[{"345":"Doctor1 Name"},{"434":"Doctor2 Name"},{"543":"Doctor3 Name"}]


        int resCount=0;
        JSONArray jArray = null;
        try {
            jArray = new JSONArray(docLst);
            resCount=jArray.length();
        }catch (Exception ex){}



        for(int i=0;i<resCount;i++) {
            {
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

                    doctors_list.add(key + " -- " + val);

                }
            }
        }

//        try {
//            jArray = new JSONArray(docLst);
//            resCount=jArray.length();
//        }
//            catch (Exception ex){}
//
//
//        for(int i=0;i<resCount;i++){
//        String res=JsonAccess.getJsonInsideArray(docLst,i);
//            doctors_list.add(res);
//        }


        //Toast.makeText(MyDoctorsActivity.this, "array index 2 is : "+res, Toast.LENGTH_LONG).show();


        //Continue from here -Sen
        arrayAdapter.notifyDataSetChanged();
    }
}
