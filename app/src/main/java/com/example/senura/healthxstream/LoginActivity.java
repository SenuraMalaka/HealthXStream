package com.example.senura.healthxstream;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.senura.healthxstream.DoctorsAct.DoctorWaitingAreaActivity;
import com.example.senura.healthxstream.EncryptAlgorithm.AES;
import com.example.senura.healthxstream.mqttConnectionPackage.JsonAccess;
import com.example.senura.healthxstream.mqttConnectionPackage.MqttConnection;
import com.example.senura.healthxstream.mqttConnectionPackage.uniqueIDgenerator;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LoginActivity extends AppCompatActivity implements MqttCallback {


    //Mqtt Var

    private MqttConnection mConnection = new MqttConnection();
    private MqttAndroidClient client = null;
    private boolean isPublished = false;
    public String SourceID;
    public String jsonResponse = null;


    private String did=null;
    private String docName=null;

    //res
    Button button_ConnectDID;
    LinearLayout linearLayout_DID;
    LinearLayout linearLayout_EmailAndPassword;
    TextView textView_DID;
    EditText editText_DID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



         if(isNetConWorking()){
             setRes();
         }


    }


    private void setRes(){
        final Button button_lg_Login;
        button_lg_Login = (Button) findViewById(R.id.button_LG_Login);

        final EditText etEmail;
        etEmail = (EditText) findViewById(R.id.editText_LG_EM);
        final EditText etPW;
        etPW = (EditText) findViewById(R.id.editText_LG_PW);


        button_ConnectDID = (Button) findViewById(R.id.button_L_DIDConnect);
        textView_DID = (TextView) findViewById(R.id.textView_L_DID);

        linearLayout_EmailAndPassword= (LinearLayout) findViewById(R.id.linearLayout_L_EmailAndPW);
        editText_DID= (EditText) findViewById(R.id.editText_LG_DID);
        linearLayout_DID = (LinearLayout) findViewById(R.id.linearLayout_L_DID);
        linearLayout_EmailAndPassword.setVisibility(View.GONE);



        button_ConnectDID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                did= editText_DID.getText().toString();
                connectMqttClient();

                linearLayout_DID.setVisibility(View.GONE);
                linearLayout_EmailAndPassword.setVisibility(View.VISIBLE);
                textView_DID.setText("Doctor ID = "+did);
            }
        });



        button_lg_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailText= etEmail.getText().toString();
                String passwordText= etPW.getText().toString();


                passAuthenticationMessage(emailText,passwordText);

//                if(emailText.equals("doc")){
//                    startActivity(new Intent(LoginActivity.this, DoctorWaitingAreaActivity.class));
//                }else {
//                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                }
//                finish();

//                try {
//                    String encryptedEmail= AES.EncryptThis(etPW.getText().toString(), etEmail.getText().toString());
//                    Log.d("Login", "encrypted email is > "+encryptedEmail);
//                    Toast.makeText(LoginActivity.this,"encrypted email is : "+encryptedEmail, Toast.LENGTH_SHORT).show();
//
//                } catch (Exception e) {
//                    Log.d("Login", e.getMessage());
//                }


            }
        });
    }



    public void passAuthenticationMessage(String email, String password) {

        if(did!=null) {
            String passingPayload = "{\"reason\":\"authentication\",\"did\":\"" + did + "\", \"email\":\""+email+"\", \"password\":\""+password+"\"}";

            String passingTopic = "healthxtream/send/";

            mConnection.publishMessage(passingPayload, passingTopic);
        }

    }




    private void setClientListenToThisAct(){
        client.setCallback(LoginActivity.this);
    }



    private void connectMqttClient() {
        client = mConnection.connect(LoginActivity.this, this, "healthxtream/doctor/"+did, false);
        client.setCallback(LoginActivity.this);
    }





    //to check the network connection is up
    private boolean isNetConWorking()
    {
        boolean isConnectionWorking = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            isConnectionWorking = true;
        }
        if(!isConnectionWorking) {
            Toast.makeText(LoginActivity.this,"No Internet Connection", Toast.LENGTH_SHORT).show();
        }

        return isConnectionWorking;

    }//isNetConWorking end


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.jsonResponse = message.toString();

        //Log.d("TagMessageArrived", jsonResponse);

        Log.d("TagMessageArrived", jsonResponse);

        String reason= JsonAccess.getJsonInsideObj(jsonResponse,"reason");




        if(reason.equals("authentication")) {
            //sample msg = {"reason":"authentication", "authenticated":"'+isPasswordValid+'"}
            authentication_resHandler(jsonResponse);

        }
    }


    private void authentication_resHandler(String res) {
        //sample msg = {"reason":"authentication", "authenticated":"'+isPasswordValid+'"}
        boolean _authenticated= Boolean.valueOf(JsonAccess.getJsonInsideObj(res,"authenticated"));

        if(_authenticated){
        did = JsonAccess.getJsonInsideObj(res, "did");
        docName = JsonAccess.getJsonInsideObj(res, "docName");
        goToDoctorWaitingAct();
        }else{
            //invalid credentials
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }



    private void goToDoctorWaitingAct(){
        //go to another act
        Intent myIntent = new Intent(LoginActivity.this, DoctorWaitingAreaActivity.class);
        myIntent.putExtra("did", did); //Optional parameters
        myIntent.putExtra("docName", docName); //Optional parameters
        DoctorWaitingAreaActivity.clientTemp=client;//setMqttclient
        DoctorWaitingAreaActivity.mConnectionTemp=mConnection;//setMqttConnection
        LoginActivity.this.startActivity(myIntent);
        finish();
    }



    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        setResult(RESULT_OK, new Intent().putExtra("EXIT", true));

                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(myIntent);
                        finish();
                    }

                }).create().show();
    }



}
