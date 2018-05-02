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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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


        connectMqttClient();
        setButtons();


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
        }

        Toast.makeText(MyDoctorsActivity.this, "response : "+jsonResponse, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    private void docIsAvailable(String jsonRes){
        //Toast.makeText(MyDoctorsActivity.this, "response : "+ JsonAccess.getJsonInsideObj(jsonResponse,"did"), Toast.LENGTH_LONG).show();
        doctors_list.add(JsonAccess.getJsonInsideObj(jsonResponse,"did"));
        arrayAdapter.notifyDataSetChanged();
    }
}
