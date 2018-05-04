package com.example.senura.healthxstream;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.senura.healthxstream.DoctorsAct.DoctorWaitingAreaActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final Button button_lg_Login;
        button_lg_Login = (Button) findViewById(R.id.button_LG_Login);

        final EditText etEmail;
        etEmail = (EditText) findViewById(R.id.editText_LG_EM);
        EditText etPW;
        etPW = (EditText) findViewById(R.id.editText_LG_PW);

        button_lg_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailText= etEmail.getText().toString();

                if(emailText.equals("doc")){
                    startActivity(new Intent(LoginActivity.this, DoctorWaitingAreaActivity.class));
                }else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                finish();
            }
        });

        isNetConWorking();

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





}
