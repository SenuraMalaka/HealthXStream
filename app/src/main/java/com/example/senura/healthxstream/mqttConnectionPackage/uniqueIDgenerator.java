package com.example.senura.healthxstream.mqttConnectionPackage;

import android.util.Log;

import java.util.UUID;

/**
 * Created by senura on 3/26/18.
 */

public class uniqueIDgenerator {


    String uniqueId = null;

    //generating UUID
    public String generateUUID(){

        //generate random UUIDs
        uniqueId = UUID.randomUUID().toString();
        Log.d("MyTag","this"+uniqueId);
        return uniqueId;
    }



}
